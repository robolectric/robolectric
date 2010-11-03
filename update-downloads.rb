#!/usr/bin/env ruby

DOWNLOADS_FILE = 'pages/download.html.md'

def need_pages_submodule
    unless File.exists?(DOWNLOADS_FILE)
        raise "Jasmine pages submodule isn't present.  Run git submodule update --init"
    end
end

def doc
    need_pages_submodule

    puts 'Creating Jasmine Documentation'
    require 'rubygems'
    require 'jsdoc_helper'

    FileUtils.rm_r "pages/jsdoc", :force => true

    JsdocHelper::Rake::Task.new(:lambda_jsdoc) do |t|
        t[:files] = jasmine_sources << jasmine_html_sources
        t[:options] = "-a"
        t[:out] = "pages/jsdoc"
        # JsdocHelper bug: template must be relative to the JsdocHelper gem, ick
        t[:template] = File.join("../".*(100), Dir::getwd, "jsdoc-template")
    end
    Rake::Task[:lambda_jsdoc].invoke
end

def fill_index_downloads
    require 'digest/sha1'

    download_html = "<!-- START_DOWNLOADS -->\n"
    Dir.glob('pages/downloads/*.jar').sort.reverse.each do |f|
        sha1 = Digest::SHA1.hexdigest File.read(f)

        fn = f.sub(/^pages\//, '')
        match = /robolectric(-all)?-([0-9].*).jar/.match(f)
        version = "SNAPSHOT"
        version = match[1] if match
        prerelease = /\.rc/.match(f)
        download_html += prerelease ? "<tr class=\"rc\">\n" : "<tr>\n"
        download_html += "  <td class=\"link\"><a href=\"#{fn}\">#{fn.sub(/downloads\//, '')}</a></td>\n"
        download_html += "  <td class=\"version\">#{version}</td>\n"
        download_html += "  <td class=\"size\">#{File.size(f) / 1024}k</td>\n"
        download_html += "  <td class=\"date\">#{File.mtime(f).strftime("%Y/%m/%d %H:%M:%S %Z")}</td>\n"
        download_html += "  <td class=\"sha\">#{sha1}</td>\n"
        download_html += "</tr>\n"
    end
    download_html += "<!-- END_DOWNLOADS -->"

    downloads_page = File.read(DOWNLOADS_FILE)
    matcher = /<!-- START_DOWNLOADS -->.*<!-- END_DOWNLOADS -->/m
    downloads_page = downloads_page.sub(matcher, download_html)
    File.open(DOWNLOADS_FILE, 'w') {|f| f.write(downloads_page)}
    puts "rewrote that file"
end

fill_index_downloads