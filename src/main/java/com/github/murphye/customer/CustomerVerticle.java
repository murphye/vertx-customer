package com.github.murphye.customer;

import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;
import org.jboss.weld.vertx.WeldVerticle;

public class CustomerVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        // Start the CDI container first
        WeldVerticle weldVerticle = new WeldVerticle();

        vertx.deployVerticle(weldVerticle, (r) -> {
            if (r.succeeded()) {
                VertxResteasyDeployment deployment = new VertxResteasyDeployment();
                deployment.setInjectorFactoryClass("org.jboss.resteasy.cdi.CdiInjectorFactory");
                deployment.start();
                deployment.getRegistry().addPerInstanceResource(CustomerService.class);

                vertx.createHttpServer().requestHandler(new VertxRequestHandler(vertx, deployment)).listen(8080, ar -> {
                    System.out.println("Server started on port " + ar.result().actualPort());
                });
            } else {
                r.cause().printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        Single<String> deployment = RxHelper.deployVerticle(Vertx.vertx(), new CustomerVerticle());

        deployment.subscribe(id -> {
            System.out.println("CustomerVerticle deployed");
        }, err -> {
            err.printStackTrace();
        });
    }
}
