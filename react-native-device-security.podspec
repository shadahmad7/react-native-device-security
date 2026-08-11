require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-device-security"
  s.version      = package["version"]
  s.summary      = "React Native Device Security TurboModule with New Architecture support"
  s.homepage     = "https://github.com/shadahmad7/react-native-device-security#readme"
  s.license      = "MIT"
  s.author       = { "Shad Ahmad" => "shad.ahmad0311@gmail.com" }
  s.platforms    = { :ios => "13.0" }

  s.source       = { :path => "." }

  s.source_files = "ios/**/*.{h,m,mm,swift}"
  s.requires_arc = true
  s.swift_version = "5.0"
  s.static_framework = true

  s.dependency "React-Core"
end