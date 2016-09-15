import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.log.SystemLogChute
import org.gradle.api.GradleException
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.*
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

public class VelocityTask extends SourceTask {

    private Map<String, Object> contextValues;
    private File outputDir;

    @Input
    @Optional
    public Map<String, Object> getContextValues() {
        return contextValues;
    }

    public void setContextValues(Map<String, Object> contextValues) {
        this.contextValues = contextValues;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @TaskAction
    public void runVelocity(IncrementalTaskInputs inputs) throws Exception {
        final VelocityEngine engine = new VelocityEngine();
        engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, SystemLogChute.class.getName());
        engine.setProperty(VelocityEngine.RESOURCE_LOADER, "file");
        engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_CACHE, "true");

        VelocityContext context = new VelocityContext();
        if (contextValues != null) {
            contextValues.each { k, v -> context.put(k, v) }
        }

        if (!inputs.incremental) {
            project.delete(outputDir.listFiles())
        }

        Set<File> outOfDateFiles = new HashSet<>()
        inputs.outOfDate { change -> outOfDateFiles.add(change.file) }

        final FileTree inputFiles = getSource();
        inputFiles.visit(new EmptyFileVisitor() {
            @Override
            public void visitFile(FileVisitDetails fvd) {
                File inputFile = fvd.file

                if (outOfDateFiles.contains(inputFile)) {
                    RelativePath inputFilePath = fvd.relativePath

                    try {
                        def outputFileName = inputFilePath.getLastName().replaceFirst("\\.vm\$", "")
                        File outputFile = inputFilePath.replaceLastName(outputFileName).getFile(getOutputDir());

                        if (logger.debugEnabled) {
                            logger.debug("Preprocessing " + inputFile + " -> " + outputFile);
                        }

                        inputFile.withReader { reader ->
                            outputFile.parentFile.mkdirs();
                            outputFile.withWriter { writer ->
                                engine.evaluate((VelocityContext) context.clone(), writer, inputFilePath.toString(), reader);
                            }
                        }
                    } catch (IOException e) {
                        throw new GradleException("Failed to process " + fvd, e);
                    }
                }
            }
        })
    }
}