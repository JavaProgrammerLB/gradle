/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.base

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.util.TextUtil

class CustomLibraryPluginIntegrationTest extends AbstractIntegrationSpec {
    def "setup"() {
        buildFile << """
import org.gradle.model.*
import org.gradle.model.collection.*

interface SampleLibrary extends LibrarySpec {}
class DefaultSampleLibrary extends DefaultLibrarySpec implements SampleLibrary {}
"""
    }

    def "plugin declares custom library"() {
        when:
        buildWithCustomComponentPlugin()
        and:
        buildFile <<"""
task checkModel << {
    assert project.projectComponents.size() == 1
    def sampleLib = project.projectComponents.sampleLib
    assert sampleLib instanceof SampleLibrary
    assert sampleLib.projectPath == project.path
    assert sampleLib.displayName == "DefaultSampleLibrary 'sampleLib'"
}
"""

        then:
        succeeds "checkModel"
    }

    def "custom component listed in components report"() {
        given:
        buildWithCustomComponentPlugin()
        when:
        succeeds "components"
        then:
        output.contains(TextUtil.toPlatformLineSeparators(""":components

------------------------------------------------------------
Root project
------------------------------------------------------------

DefaultSampleLibrary 'sampleLib'
--------------------------------

Source sets
    No source sets.

Binaries
    No binaries.

Note: currently not all plugins register their components, so some components may not be visible here.

BUILD SUCCESSFUL"""))
    }


    def buildWithCustomComponentPlugin() {
        buildFile << """
        class MySamplePlugin implements Plugin<Project> {
            void apply(final Project project) {}

            @RuleSource
            @ComponentModel(type = SampleLibrary.class, implementation = DefaultSampleLibrary.class)
            static class Rules {
                @Mutate
                void createSampleLibraryComponents(NamedItemCollectionBuilder<SampleLibrary> componentSpecs) {
                    componentSpecs.create("sampleLib")
                }
            }
        }

        apply plugin:MySamplePlugin
        """
    }
}
