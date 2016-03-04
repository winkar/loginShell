#!/usr/bin/env ruby
require 'appium_lib'

APP_PATH = './apk/taobao.apk'

desired_caps = {
    caps:  {
        platformName: 'Android',
        deviceName: 'Motorola Moto X',
        app: APP_PATH
    },
    appium_lib: {
        sauce_username: nil,
        sauce_access_key: nil
    }
}



driver = Appium::Driver.new(desired_caps).start_driver
