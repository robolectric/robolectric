#!/usr/bin/env ruby

DOWNLOADS_FILE = 'pages/download.md'

def need_pages_submodule
    unless File.exists?(DOWNLOADS_FILE)
        raise "Robolectric pages submodule isn't present.  Run git submodule update --init"
    end
end

def fill_index_downloads
    require 'digest/sha1'

    download_html = "<!-- START_DOWNLOADS -->\n"
    Dir.glob('pages/downloads/*.jar').sort.reverse.each do |f|
        sha1 = Digest::SHA1.hexdigest File.read(f)

        fn = f.sub(/^pages\//, '')
        match = /robolectric-?([0-9]\.[0-9](\.[0-9])?)?(-all)?(-src)?\.jar/.match(f)
        version = match[1] if match
        version = "SNAPSHOT" unless version
        prerelease = /\.rc/.match(f)
        download_html += prerelease ? "<tr class=\"rc\">\n" : "<tr>\n"
        download_html += "  <td class=\"link\"><a href=\"#{fn}\" onClick=\"javascript:pageTracker._trackPageView('#{fn}'); \">#{fn.sub(/downloads\//, '')}</a></td>\n"
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
    puts "rewrote " + DOWNLOADS_FILE
end

fill_index_downloads