source "https://rubygems.org"

gem "fastlane"
gem "rake"

# Fastlane plugins are managed via fastlane/Pluginfile
# They will be installed when bundle install runs after evaluating the Pluginfile
plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)

