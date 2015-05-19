/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.java;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.apache.commons.lang.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.AbstractBuildableModelElement;
import org.gradle.api.internal.ClosureBackedAction;
import org.gradle.language.base.internal.LanguageSourceSetInternal;
import org.gradle.platform.base.DependencySpecContainer;
import org.gradle.platform.base.internal.DefaultDependencySpecContainer;

public abstract class AbstractLanguageSourceSet extends AbstractBuildableModelElement implements LanguageSourceSetInternal {
    private final String name;
    private final String fullName;
    private final String displayName;
    private final SourceDirectorySet source;
    private final DependencySpecContainer dependencies;
    private boolean generated;
    private Task generatorTask;

    public AbstractLanguageSourceSet(String name, String parentName, String typeName, SourceDirectorySet source) {
        this.name = name;
        this.fullName = parentName + StringUtils.capitalize(name);
        this.displayName = String.format("%s '%s:%s'", typeName, parentName, name);
        this.source = source;
        this.dependencies = new DefaultDependencySpecContainer();
        super.builtBy(source.getBuildDependencies());
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public void builtBy(Object... tasks) {
        generated = true;
        super.builtBy(tasks);
    }

    public void generatedBy(Task generatorTask) {
        this.generatorTask = generatorTask;
    }

    public Task getGeneratorTask() {
        return generatorTask;
    }

    public boolean getMayHaveSources() {
        // TODO:DAZ This doesn't take into account build dependencies of the SourceDirectorySet.
        // Should just ditch SourceDirectorySet from here since it's not really a great model, and drags in too much baggage.
        return generated || !source.isEmpty();
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public void source(Action<? super SourceDirectorySet> config) {
        config.execute(getSource());
    }

    public SourceDirectorySet getSource() {
        return source;
    }

    @Override
    public DependencySpecContainer getDependencies() {
        return dependencies;
    }

    @Override
    public DependencySpecContainer dependencies(Action<? super DependencySpecContainer> configureAction) {
        configureAction.execute(getDependencies());
        return getDependencies();
    }

    public DependencySpecContainer dependencies(@DelegatesTo(value = DependencySpecContainer.class, strategy = Closure.DELEGATE_FIRST) Closure dependenciesSpec) {
        return dependencies(new ClosureBackedAction<DependencySpecContainer>(dependenciesSpec));
    }

}
