package com.wibeechat.missa.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualService {
    private String apiVersion = "networking.istio.io/v1alpha3";
    private String kind = "VirtualService";
    private ObjectMeta metadata;
    private VirtualServiceSpec spec;

    @Data
    public static class VirtualServiceSpec {
        private List<String> hosts;
        private List<String> gateways;
        private List<HTTPRoute> http;
    }

    @Data
    public static class HTTPRoute {
        private List<HTTPMatchRequest> match;
        private List<HTTPRouteDestination> route;
        private HTTPRetry retries;
        private String timeout;
        private HTTPFaultInjection fault;
    }

    @Data
    public static class HTTPMatchRequest {
        private String uri;
        private String method;
        private Map<String, StringMatch> headers;
    }

    @Data
    public static class StringMatch {
        private String exact;
        private String prefix;
        private String regex;
    }

    @Data
    public static class HTTPRouteDestination {
        private Destination destination;
        private Integer weight;
    }

    @Data
    public static class Destination {
        private String host;
        private String subset;
        private Integer port;
    }

    @Data
    public static class HTTPRetry {
        private Integer attempts;
        private String perTryTimeout;
    }

    @Data
    public static class HTTPFaultInjection {
        private Delay delay;
        private Abort abort;
    }

    @Data
    public static class Delay {
        private String fixedDelay;
        private Integer percent;
    }

    @Data
    public static class Abort {
        private Integer httpStatus;
        private Integer percent;
    }
}