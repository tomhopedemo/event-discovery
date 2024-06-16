package com.arcta.events;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.seda.SedaComponent;
import org.apache.camel.impl.DefaultCamelContext;
import java.util.Map;

import static com.arcta.events.Util.readString;
import static org.apache.camel.Exchange.*;
class ApiServer {
    final Dirs dirs;
    ApiServer(Dirs dirs) {
        this.dirs = dirs;
    }
    void run() {
        CamelContext context = new DefaultCamelContext();
        try { configQueue(context);
            context.addRoutes(new RouteBuilder() {
                public void configure() { //</p><p>e.g. curl -X POST "https://api.londonoo.com:8888" -d "city=london&date=2022-05-01"</p></div></body></html>
                    from("jetty:http://0.0.0.0:8888/").to("sedaComponent:request?timeout=0&concurrentConsumers=3");
                    from("sedaComponent:request").process(new RestRequestProcessor());}});
            context.start();
            while (true) {
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException ignored) {}
            }
        }
        catch (Exception ignored){
        } finally {
            context.stop();
        }
    }

    class RestRequestProcessor implements Processor {
        public void process(Exchange exchange)  {
            try { String date = exchange.getIn().getHeader("date", String.class);
                if (Util.empty(date) || !date.matches("[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}")){
                    exchange.getMessage().setHeaders(Map.of(HTTP_RESPONSE_CODE, "500", CONTENT_TYPE, "text/plain"));
                    exchange.getMessage().setBody("Unable to parse date\n");return;
                }
                String response = readString(dirs.getTsvDir() + date + ".tsv");
                if (!Util.empty(response)){
                    response = response + "\n";
                }
                exchange.getIn().setBody(response);
            } catch (Exception ignored){}
        }
    }
    void configQueue(CamelContext ctx) {
        SedaComponent c = new SedaComponent();
        c.setQueueSize(3);
        ctx.addComponent("sedaComponent", c);
    }
}