/*
 * This file is part of fabric-loom, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016, 2017, 2018 FabricMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.fabricmc.loom;

import net.fabricmc.loom.transformers.ContainedZipStrippingTransformer;
import net.fabricmc.loom.transformers.TransformerProjectManager;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;

public class LoomGradlePlugin implements Plugin<Project>
{
	@Override
	public void apply(Project target)
    {
        final Attribute<Boolean> stripped = Attribute.of("stripped", Boolean.class);

        TransformerProjectManager.getInstance().register(target);

        target.getDependencies().attributesSchema(schema -> {
            schema.attribute(stripped);
        });

        target.getDependencies().artifactTypes(types -> {
            types.all(type -> {
                target.getLogger().lifecycle("[Loom]: Available: " + type.getName());
            });

            types.getByName("jar", jarType -> {
                jarType.getAttributes().attribute(stripped, false);
            });
        });

        target.getDependencies().registerTransform(
          ContainedZipStrippingTransformer.class,
          config -> {
              config.getFrom().attribute(stripped, false);
              config.getTo().attribute(stripped, true);
              config.parameters(parameters -> {
                  parameters.getProjectPathParameter().set(target.getPath());
              });
          }
        );

        target.getConfigurations().all(config -> {
            if (config.isCanBeResolved())
            {
                config.attributes(container -> {
                    container.attribute(stripped, true);
                });
            }
        });
    }
}
