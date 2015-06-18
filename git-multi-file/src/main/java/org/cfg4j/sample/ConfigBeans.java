/*
 * Copyright 2015 Norbert Potocki (norbert.potocki@nort.pl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cfg4j.sample;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.Environment;
import org.cfg4j.source.context.ImmutableEnvironment;
import org.cfg4j.source.git.ConfigFilesProvider;
import org.cfg4j.source.git.GitConfigurationSourceBuilder;
import org.cfg4j.source.refresh.RefreshStrategy;
import org.cfg4j.source.refresh.strategy.PeriodicalRefreshStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class ConfigBeans {

  @Value("${configRepoPath:https://github.com/cfg4j/cfg4j-git-sample-config.git}")
  private String configRepoPath; // Run with -DconfigRepoPath=<repositoryUrl> parameter to override

  @Value("${configBranch:production-env}")
  private String branch; // Run with -DconfigBranch=<branchName> parameter to override

  @Bean
  public ConfigurationProvider configurationProvider() {
    // Specify which files to load. Configuration from both files will be merged.
    ConfigFilesProvider configFilesProvider = () -> Arrays.asList(new File("application.properties"), new File("otherConfig.properties"));

    // Use Git repository as configuration store
    ConfigurationSource source = new GitConfigurationSourceBuilder()
        .withRepositoryURI(configRepoPath)
        .withConfigFilesProvider(configFilesProvider)
        .build();

    // Select branch to use (use new DefaultEnvironment()) for master
    Environment environment = new ImmutableEnvironment(branch);

    // Reload configuration every 5 seconds
    RefreshStrategy refreshStrategy = new PeriodicalRefreshStrategy(5, TimeUnit.SECONDS);

    // Create provider
    return new ConfigurationProviderBuilder()
        .withConfigurationSource(source)
        .withEnvironment(environment)
        .withRefreshStrategy(refreshStrategy)
        .build();
  }
}
