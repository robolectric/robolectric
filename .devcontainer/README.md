# GitHub's Codespaces Configuration

GitHub Codespaces is the vscode-based cloud development environment for GitHub
projects. We can use [devcontainer][devcontainer-introduction] to configure
containers for GitHub Codespaces when opening Robolectric project from it.

As Robolectric there are special requirements for Android SDK and `ANDROID_SDK_HOME`
environment variable for building, Robolectric selects
[`Dockerfile`][devcontainer-introduction-docker] to configure necessary Android
environment for itself.

Any contributor can update it based on the demand. When we update `Dockerfile`, we can run
the following command to build and test it locally:

```shell
cd .devcontainer
docker buildx build .
```

> See [Docker's buildx repository][docker-buildx] to install buildx.

When local testing is passed, we can push it to remote custom branch, and checkout it
with latest change in GitHub's Codespaces page and then trigger "Rebuild Container" to
test its configuration in GitHub's Codespaces environment.

If everything goes well, sending the PR and wait the merging.

Because devcontainer is brought by VSCode, this configuration can also be used
for VSCode.

[devcontainer-introduction]: https://docs.github.com/en/codespaces/setting-up-your-project-for-codespaces/adding-a-dev-container-configuration/introduction-to-dev-containers
[devcontainer-introduction-docker]: https://docs.github.com/en/codespaces/setting-up-your-project-for-codespaces/adding-a-dev-container-configuration/introduction-to-dev-containers#dockerfile
[docker-buildx]: https://github.com/docker/buildx
