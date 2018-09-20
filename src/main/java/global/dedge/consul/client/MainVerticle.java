package global.dedge.consul.client;

import java.util.Arrays;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.Service;
import io.vertx.ext.consul.ServiceOptions;

public class MainVerticle extends AbstractVerticle {

	public static void main(String... args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(MainVerticle.class.getName());
	}
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Future<Void> steps = registerService();//.compose(v -> registerService());//registerService();//prepareDataBase().compose(v -> startHttpServer());
		steps.setHandler(startFuture.completer());
	}

	private Future<Void> listServices() {
		Future<Void> future = Future.future();
		ConsulClient consulClient = ConsulClient.create(vertx);
		consulClient.catalogServiceNodes("serviceTino", res -> {
			if (res.succeeded()) {
				System.out.println("found " + res.result().getList().size() + " services");
				System.out.println("consul state index: " + res.result().getIndex());
				for (Service service : res.result().getList()) {
					System.out.println("Service node: " + service.getNode());
					System.out.println("Service address: " + service.getAddress());
					System.out.println("Service port: " + service.getPort());
				}
			} else {
				res.cause().printStackTrace();
			}
		});

		future.complete();
		return future;
	}

	private Future<Void> registerService() {
		Future<Void> future = Future.future();
		ConsulClient consulClient = ConsulClient.create(vertx);
		JsonObject checkOptions = new JsonObject("{\"check\": {\"name\": \"ping\",\n" + 
				"  \"args\": [\"ping\", \"-c1\", \"google.com\"], \"interval\": \"10s\"}}");
		ServiceOptions opts = new ServiceOptions().setName("serviceTino").setId("serviceTinoId")
				
				.setTags(Arrays.asList("Tino", "Testing")).setCheckOptions(new CheckOptions(checkOptions))
				.setAddress("127.0.0.1").setPort(8301);

		consulClient.registerService(opts, res -> {
			if (res.succeeded()) {
				System.out.println("Service successfully registered");
			} else {
				res.cause().printStackTrace();
			}

		});

		future.complete();
		return future;
	}

	private Future<Void> deregisterService() {
		Future<Void> future = Future.future();
		ConsulClient consulClient = ConsulClient.create(vertx);
		consulClient.deregisterService("serviceTinoId", res -> {
			if (res.succeeded()) {
				System.out.println("Service successfully deregistered");
			} else {
				res.cause().printStackTrace();
			}
		});
		future.complete();

		return future;
	}
}
