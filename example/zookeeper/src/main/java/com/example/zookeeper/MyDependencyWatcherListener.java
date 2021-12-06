package com.example.zookeeper;

import org.springframework.cloud.zookeeper.discovery.watcher.DependencyState;
import org.springframework.cloud.zookeeper.discovery.watcher.DependencyWatcherListener;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-11-12
 */

@Component
public class MyDependencyWatcherListener implements DependencyWatcherListener {


    /**
     * Method executed upon state change of a dependency.
     *
     * @param dependencyName - alias from microservice configuration
     * @param newState       - new state of the dependency
     */
    @Override
    public void stateChanged(String dependencyName, DependencyState newState) {

    }
}
