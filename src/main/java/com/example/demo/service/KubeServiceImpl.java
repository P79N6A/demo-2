package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.Pair;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;


/**
 * @author zhaozi on 2019/3/21
 */
@Service @Log4j2 public class KubeServiceImpl implements KubeService {

    private ApiClient client;

    private Map<String, StringBuffer> logBufferMap = new ConcurrentHashMap<>();
    private Map<String, StringBuffer> totalLogMap = new ConcurrentHashMap<>();

    private CoreV1Api api = new CoreV1Api();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2, 10,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));

    @PostConstruct public void init() throws IOException {
        client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        api.setApiClient(client);
    }

    @Override public boolean runTask(String id) throws ApiException, IOException, InterruptedException {

        log.info("Running Task Start");

        String metaName = id;

        String buildTemplate = "jenkins-maven-auto-define";
        String body =
                "{\"kind\":\"ProwJob\",\"apiVersion\":\"prow.k8s.io/v1\",\"metadata\":{\"name\":\""
                + metaName
                + "\",\"creationTimestamp\":null,\"labels\":{\"created-by-prow\":\"true\",\"prow.k8s.io/job\":\"\",\"prow.k8s.io/refs.org\":\"app_release\",\"prow.k8s.io/refs.repo\":\"cqplus\",\"prow.k8s.io/type\":\"postsubmit\"},\"annotations\":{\"prow.k8s.io/job\":\"\"}},\"spec\":{\"type\":\"postsubmit\",\"agent\":\"knative-build\",\"refs\":{\"org\":\"app_release\",\"repo\":\"cqplus\",\"base_ref\":\"master\"},\"build_spec\":{\"source\":{\"git\":{\"url\":\"http://gitlab-test.alipay.net/app_release/cqplus.git\",\"revision\":\"master\"}},\"serviceAccountName\":\"knative-build-bot\",\"template\":{\"name\":\""+buildTemplate+"\",\"env\":[{\"name\":\"SOURCE_URL\",\"value\":\"http://gitlab-test.alipay.net/app_release/cqplus.git\"},{\"name\":\"REPO_OWNER\",\"value\":\"app_release\"},{\"name\":\"REPO_NAME\",\"value\":\"cqplus\"},{\"name\":\"JENKINS_FILE\",\"value\":\"jenkinsFile\"},"
                + "{\"name\":\"ARTIFACT_DOMAIN\",\"value\":\"http://192.168.64.1:9001\"},{\"name\":\"ARTIFACT_FOLDER\",\"value\":\"/artifact\"},{\"name\":\"JOB_ID\",\"value\":\""+ metaName+"\"}]}}},\"status\":{\"startTime\":null,\"state\":\"pending\"}}\n";

        JSONObject jsonObject = JSONObject.parseObject(body);

        List<Pair> localVarQueryParams = new ArrayList();
        List<Pair> localVarCollectionQueryParams = new ArrayList();
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Accept", "application/json, */*");
        Request post = client.buildRequest("/apis/prow.k8s.io/v1/namespaces/jx/prowjobs", "POST",
                        localVarQueryParams, localVarCollectionQueryParams, jsonObject, header,
                        null, new String[] { "BearerToken" }, null);

        Call call = client.getHttpClient().newCall(post);
        Response execute = call.execute();
        ResponseBody body1 = execute.body();
        Object result = JSONObject.parseObject(body1.bytes(), JSONObject.class);

        if (execute.isSuccessful()) {
            threadPoolExecutor.execute(new LogTask(metaName));
        }

        log.info("running finish {} successfull {}", result, execute.isSuccessful());
        return execute.isSuccessful();
    }

    @Override public String getLogger(String id) throws ApiException, IOException {
        if(Objects.isNull(logBuffer(id))) {
            return "";
        }
        int length = logBuffer(id).length();
        String content = logBuffer(id).substring(0, length);
        logBuffer(id).delete(0, length);
        return content;
    }

    @Override public String downloadLogger(String id) {
        if(Objects.isNull(totalLog(id))) {
            return "";
        }
        return totalLog(id).toString();
    }

    class LogTask implements Runnable {

        private String metaName;

        public LogTask(String metaName) {
            this.metaName = metaName;
        }

        @Override public void run() {

            try {
                boolean flag = true;
                while (flag) {

                    log.info("Start fetching log pod {}", metaName);
                    V1PodList pods = api
                            .listPodForAllNamespaces(null, null, null, null, null, null, null, null,
                                    null);

                    V1Pod distPod = null;
                    for (V1Pod item : pods.getItems()) {

                        if (item.getMetadata().getName().contains(metaName)) {
                            distPod = item;
                        }
                    }

                    if (!Objects.isNull(distPod)) {
                        flag = false;

                        log.info("found pod {} start logging", metaName);

                        PodLogs logs = new PodLogs();
                        for (V1Container initContainer : distPod.getSpec().getInitContainers()) {
                            InputStream is = logs
                                    .streamNamespacedPodLog("jx", distPod.getMetadata().getName(),
                                            initContainer.getName(), 10, 1, true);
//                            ByteStreams.copy(is, System.out);
                            copy(is, metaName);
                        }

                        log.info("finish pod {}  logging", metaName);
                    } else {

                        log.info("Not found pod {}, retry..", metaName);
                    }

                    Thread.sleep(3000);
                }

            } catch (Throwable throwable) {
                log.error(throwable.getMessage());
            }
        }
    }

    @CanIgnoreReturnValue
    public long copy(InputStream from, String id) throws IOException {
        Preconditions.checkNotNull(from);
        byte[] buf = createBuffer();
        long total = 0L;

        while(true) {
            int r = from.read(buf);
            String content = IOUtils.toString(buf, Charsets.UTF_8.toString()).replaceAll("\u0000", "");
            System.out.print(content);

            logBuffer(id).append(content);
            totalLog(id).append(content);

            buf = createBuffer();
            //            System.out.println(IOUtils.toString(buf, Charsets.UTF_8.toString()));

            if (r == -1) {
                return total;
            }

            total += (long)r;
        }
    }
    byte[] createBuffer() {
        return new byte[8192];
    }

    public StringBuffer logBuffer(String id) {

        if(Objects.isNull(logBufferMap.get(id))) {
            logBufferMap.put(id, new StringBuffer());
        }
        return logBufferMap.get(id);
    }

    public StringBuffer totalLog(String id) {
        if(Objects.isNull(totalLogMap.get(id))) {
            totalLogMap.put(id, new StringBuffer());
        }
        return totalLogMap.get(id);
    }

}
