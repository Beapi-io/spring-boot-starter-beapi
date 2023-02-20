#!/usr/bin/env groovy

/*
 * Copyright 2013-2023 Beapi.io
 *
 * Licensed under the MPL-2.0 License;
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//package io.beapi.api.cli

//@Grab(group='commons-cli', module='commons-cli', version='1.4')

import groovy.cli.commons.CliBuilder
import groovy.json.JsonSlurper
import java.text.DecimalFormat
import java.util.regex.Matcher

class Beapi {

    // expect 2 args
    static void main(String[] args) {
        CommandLineInterface cli = CommandLineInterface.INSTANCE
        cli.parse(args)
    }
}


/**
 * CLI Interface for tool
 */
enum CommandLineInterface{
    INSTANCE

    CliBuilder cliBuilder

    CommandLineInterface(){
        cliBuilder = new CliBuilder(
            usage: 'Beapi [<options>] -controller -domainname=<classname>',
            header: 'OPTIONS:',
            footer: "Beapi is a CLI tool for the Beapi starter to scaffold controllers, domains, and/or connectors for your API project. If you have any questions, please visit us a http://beapi.io. Thanks again."
        )

        cliBuilder.width=80
        cliBuilder.with {

            // HELP OPT
            h(longOpt: 'help', 'Print this help text and exit (usage: -h, --help)')

            // REQUIRED TEST OPTS
            d(longOpt:'method',args:2, valueSeparator:'=',argName:'property=value', 'request method for endpoint (GET/PUT/POST/DELETE)')
            _(longOpt:'domainname',args:2, valueSeparator:'=',argName:'property=value', 'url for making the api call (usage: --endpoint=http://localhost:8080)')

            _(longOpt:'controller',args:1,argName:'value', 'scaffold value (usage: -controller)')
            _(longOpt:'domain',args:1,argName:'value', 'scaffold value (usage: -domain)')
            _(longOpt:'connector',args:1,argName:'value', 'scaffold value (usage: -connector)')
        }
    }

    void parse(args) {
        OptionAccessor options = cliBuilder.parse(args)
        try {


            if (!args) {
                throw new Exception('Could not parse command line options.\n')
            }
            if (options.h) {
                cliBuilder.usage()
                System.exit 0
            }

            if (options.d) {
                if (options.controller) {
                    try {
                        createController()
                    } catch (Exception e) {
                        throw new Exception('Scaffold Value must not be NULL. Please provide ARG value of \'-controller/domain/connector\'.', e)
                    }
                }

                if (options.domain) {
                    try {
                        createDomain()
                    } catch (Exception e) {
                        throw new Exception('Scaffold Value must not be NULL. Please provide ARG value of \'-controller/domain/connector\'.', e)
                    }
                }

                if (options.connector) {
                    try {
                        createConnector()
                    } catch (Exception e) {
                        throw new Exception('Scaffold Value must not be NULL. Please provide ARG value of \'-controller/domain/connector\'.', e)
                    }
                }
            } else {
                throw new Exception('Method (-d/--domainname) is REQUIRED for Beapi to work. Please try again.\n')
            }
        } catch (Exception e) {
            System.err << e
            System.exit 1
        }
    }

    private void createController(){

    }

    private void createDomain(){

    }

    private void createConnector(){

    }

}













