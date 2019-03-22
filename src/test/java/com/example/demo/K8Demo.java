package com.example.demo;

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
import java.util.UUID;
import org.apache.maven.surefire.shade.org.apache.commons.io.IOUtils;

/**
 * @author zhaozi on 2019/3/21
 */
public class K8Demo {

    public static void main(String[] args) throws IOException, ApiException, InterruptedException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

//        PodLogs logs = new PodLogs();
//        V1Pod pod =
//                api
//                        .listNamespacedPod("jx", null, "false", null, null, null, null, null, null, null)
//                        .getItems()
//                        .get(0);
//
//        InputStream is = logs.streamNamespacedPodLog(pod);
//        ByteStreams.copy(is, System.out);

//        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
//        for (V1Pod item : list.getItems()) {
//            System.out.println(item.getMetadata().getName());
//        }

        String podName = UUID.randomUUID().toString();

        String body = "{\"kind\":\"ProwJob\",\"apiVersion\":\"prow.k8s.io/v1\",\"metadata\":{\"name\":\"" + podName + "\",\"creationTimestamp\":null,\"labels\":{\"created-by-prow\":\"true\",\"prow.k8s.io/job\":\"\",\"prow.k8s.io/refs.org\":\"app_release\",\"prow.k8s.io/refs.repo\":\"cqplus\",\"prow.k8s.io/type\":\"postsubmit\"},\"annotations\":{\"prow.k8s.io/job\":\"\"}},\"spec\":{\"type\":\"postsubmit\",\"agent\":\"knative-build\",\"refs\":{\"org\":\"app_release\",\"repo\":\"cqplus\",\"base_ref\":\"master\"},\"build_spec\":{\"source\":{\"git\":{\"url\":\"http://gitlab-test.alipay.net/app_release/cqplus.git\",\"revision\":\"master\"}},\"serviceAccountName\":\"knative-build-bot\",\"template\":{\"name\":\"jenkins-maven\",\"env\":[{\"name\":\"SOURCE_URL\",\"value\":\"http://gitlab-test.alipay.net/app_release/cqplus.git\"},{\"name\":\"REPO_OWNER\",\"value\":\"app_release\"},{\"name\":\"REPO_NAME\",\"value\":\"cqplus\"},{\"name\":\"JENKINS_FILE\",\"value\":\"jenkinsFile\"}]}}},\"status\":{\"startTime\":null,\"state\":\"pending\"}}\n";

        JSONObject jsonObject = JSONObject.parseObject(body);

        List<Pair> localVarQueryParams = new ArrayList();
        List<Pair> localVarCollectionQueryParams = new ArrayList();
        Map<String, String> header = new HashMap<>();
//        header.put("Accept", "application/json");
        header.put("Content-Type", "application/json");
        header.put("Accept", "application/json, */*");
        Request post = api.getApiClient()
                .buildRequest("/apis/prow.k8s.io/v1/namespaces/jx/prowjobs", "POST", localVarQueryParams, localVarCollectionQueryParams,
                        jsonObject, header, null, new String[] {"BearerToken"}, null);

        Call call = client.getHttpClient().newCall(post);
        Response execute = call.execute();
        ResponseBody body1 = execute.body();

        Object result = JSONObject.parseObject(body1.bytes(), JSONObject.class);

        Thread.sleep(5000);

        V1PodList pods = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

        V1Pod distPod = null;
        for (V1Pod item : pods.getItems()) {
            if(item.getMetadata().getName().contains(podName)) {
                distPod = item;
            }
        }

        PodLogs logs = new PodLogs();
        for (V1Container initContainer : distPod.getSpec().getInitContainers()) {
            InputStream is = logs.streamNamespacedPodLog("jx", distPod.getMetadata().getName(), initContainer.getName());
//            ByteStreams.copy(is, System.out);
            copy(is);
        }
    }

    @CanIgnoreReturnValue
    public static long copy(InputStream from) throws IOException {
        Preconditions.checkNotNull(from);
        byte[] buf = createBuffer();
        long total = 0L;

        while(true) {
            int r = from.read(buf);
            System.out.print(IOUtils.toString(buf, Charsets.UTF_8.toString()).replaceAll("\u0000", ""));
            buf = createBuffer();
            //            System.out.println(IOUtils.toString(buf, Charsets.UTF_8.toString()));

            if (r == -1) {
                return total;
            }

            total += (long)r;
        }
    }
    static byte[] createBuffer() {
        return new byte[8192];
    }

}
