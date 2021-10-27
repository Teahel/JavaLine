package com.example.zookeeper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class ZookeeperApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZookeeperApplication.class, args);
	}

	@GetMapping("/helloworld")
	public String helloWorld() {
		return "Hello World!";
	}


	@Autowired
	private DiscoveryClient discoveryClient;

	public String serviceUrl() {
		List<ServiceInstance> list = discoveryClient.getInstances("STORES");
		if (list != null && list.size() > 0 ) {
			return list.get(0).getUri().toString();
		}
		return null;
	}
}
