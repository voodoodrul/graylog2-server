/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.restclient.models;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog2.rest.models.system.inputs.requests.InputLaunchRequest;
import org.graylog2.rest.models.system.inputs.responses.InputCreated;
import org.graylog2.rest.models.system.inputs.responses.InputStateSummary;
import org.graylog2.rest.models.system.inputs.responses.InputSummary;
import org.graylog2.rest.models.system.inputs.responses.InputTypeInfo;
import org.graylog2.rest.models.system.inputs.responses.InputsList;
import org.graylog2.rest.models.system.metrics.responses.MetricsSummaryResponse;
import org.graylog2.rest.models.system.responses.SystemOverviewResponse;
import org.graylog2.restclient.lib.APIException;
import org.graylog2.restclient.lib.ApiClient;
import org.graylog2.restclient.lib.ExclusiveInputException;
import org.graylog2.restclient.lib.metrics.Metric;
import org.graylog2.restclient.models.api.responses.cluster.RadioSummaryResponse;
import org.graylog2.restclient.models.api.responses.system.InputTypeSummaryResponse;
import org.graylog2.restroutes.factories.RestAdapterFactory;
import org.graylog2.restroutes.generated.radio.RadioAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.client.Response;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class Radio extends ClusterEntity {

    public interface Factory {
        Radio fromSummaryResponse(RadioSummaryResponse r);
    }

    private static final Logger LOG = LoggerFactory.getLogger(Radio.class);

    private final ApiClient api;
    private final Input.Factory inputFactory;
    private final RestAdapterFactory restAdapterFactory;
    private final URI transportAddress;

    private String id;
    private String shortNodeId;

    private NodeJVMStats jvmInfo;
    private SystemOverviewResponse systemInfo;
    private BufferInfo bufferInfo;

    @AssistedInject
    public Radio(ApiClient api, Input.Factory inputFactory, RestAdapterFactory restAdapterFactory, @Assisted RadioSummaryResponse r) {
        this.api = api;
        this.inputFactory = inputFactory;
        this.restAdapterFactory = restAdapterFactory;

        transportAddress = normalizeUriPath(r.transportAddress);
        id = r.nodeId;
        shortNodeId = r.shortNodeId;
    }

    public synchronized void loadSystemInformation() {
        if (systemInfo != null) {
            return;
        }
        try {
            systemInfo = api().system().system();
        } catch (Exception e) {
            LOG.error("Unable to load system information for radio " + this, e);
        }
    }

    public synchronized void loadJVMInformation() {
        if (jvmInfo != null) {
            return;
        }
        try {
            jvmInfo = new NodeJVMStats(api().system().jvm());
        } catch (Exception e) {
            LOG.error("Unable to load JVM information for radio " + this, e);
        }
    }

    public synchronized void loadBufferInformation() {
        if (bufferInfo != null) {
            return;
        }
        try {
            bufferInfo = new BufferInfo(api().buffers().utilization());
        } catch (Exception e) {
            LOG.error("Unable to load buffer information for radio " + this, e);
        }
    }

    public String getNodeId() {
        return id;
    }

    @Override
    public String getShortNodeId() {
        return shortNodeId;
    }

    public String getId() {
        return id;
    }

    public NodeJVMStats jvm() {
        loadJVMInformation();

        if (jvmInfo == null) {
            return NodeJVMStats.buildEmpty();
        } else {
            return jvmInfo;
        }
    }

    public String getPid() {
        return jvm().getPid();
    }

    public String getJVMDescription() {
        return jvm().getInfo();
    }

    public void overrideLbStatus(String override) throws APIException, IOException {
        api().loadBalancerStatus().override(override);
    }

    @Override
    public boolean launchExistingInput(String inputId) {
        try {
            final Response response = api().inputs().launchExisting(inputId);
            return (response.getStatus() == 202);
        } catch (Exception e) {
            LOG.error("Could not launch input " + inputId, e);
        }

        return false;
    }

    @Override
    public boolean terminateInput(String inputId) {
        try {
            return (api().inputs().terminate(inputId).getStatus() == 202);
        } catch (Exception e) {
            LOG.error("Could not terminate input " + inputId, e);
        }

        return false;
    }

    private SystemOverviewResponse systemInfo() {
        loadSystemInformation();

        if (systemInfo == null) {
            return SystemOverviewResponse.buildEmpty();
        } else {
            return systemInfo;
        }
    }

    @Override
    public String getTransportAddress() {
        return transportAddress.toASCIIString();
    }

    public URI getTransportAddressUri() {
        return transportAddress;
    }

    @Override
    public String getHostname() {
        return systemInfo().hostname();
    }

    public String getVersion() {
        return systemInfo().version();
    }

    public String getLifecycle() {
        return this.systemInfo().lifecycle();
    }

    public boolean lbAlive() {
        final SystemOverviewResponse info = systemInfo();

        return info.lbStatus() != null && info.lbStatus().equals("alive");
    }

    @Override
    public void touch() {
        // We don't do touches against radios.
    }

    @Override
    public void markFailure() {
        // No failure counting in radios for now.
    }

    public Map<String, InputTypeSummaryResponse> getAllInputTypeInformation() throws IOException, APIException {
        Map<String, InputTypeSummaryResponse> types = Maps.newHashMap();

        for (String type : getInputTypes().keySet()) {
            InputTypeSummaryResponse itr = getInputTypeInformation(type);
            types.put(itr.type, itr);
        }

        return types;
    }

    public Map<String, String> getInputTypes() throws IOException, APIException {
        return api().inputTypes().types().types();
    }

    public InputTypeInfo getInputTypeInformation(String type) throws IOException, APIException {
        return api().inputTypes().info(type);
    }

    public List<Input> getInputs() {
        List<Input> inputs = Lists.newArrayList();

        for (InputStateSummary input : inputs().inputs()) {
            inputs.add(inputFactory.fromSummaryResponse(input.messageInput(), this));
        }

        return inputs;
    }

    public Input getInput(String inputId) throws IOException, APIException {
        final InputSummary inputSummaryResponse = api().inputs().single(inputId);
        return inputFactory.fromSummaryResponse(inputSummaryResponse, this);
    }


    public int numberOfInputs() {
        return inputs().total();
    }

    private InputsList inputs() {
        try {
            return api().inputs().list();
        } catch (Exception e) {
            LOG.error("Could not get inputs.", e);
            throw new RuntimeException("Could not get inputs.", e);
        }
    }

    @Override
    public InputCreated launchInput(String title, String type, Boolean global, Map<String, Object> configuration, boolean isExclusive, String nodeId) throws ExclusiveInputException {
        if (isExclusive) {
            for (Input input : getInputs()) {
                if (input.getType().equals(type)) {
                    throw new ExclusiveInputException();
                }
            }
        }

        final InputLaunchRequest request = InputLaunchRequest.create(title, type, global, configuration, nodeId);

        try {
            return api().inputs().create(request);
        } catch (Exception e) {
            LOG.error("Could not launch input " + title, e);
            return null;
        }
    }

    public BufferInfo getBuffers() {
        loadBufferInformation();

        if (bufferInfo == null) {
            return BufferInfo.buildEmpty();
        } else {
            return bufferInfo;
        }
    }

    public String getThreadDump() throws IOException, APIException {
        return api().system().threaddump();
    }

    public long getThroughput() {
        return api().throughput().total().throughput();
    }

    public Map<String, Metric> getMetrics(String namespace) throws APIException, IOException {
        MetricsSummaryResponse response = api().metrics().byNamespace(namespace);

        return response.metrics();
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        if (id == null) {
            b.append("UnresolvedNode {'").append(transportAddress).append("'}");
            return b.toString();
        }

        b.append("Node {");
        b.append("'").append(id).append("'");
        b.append(", ").append(transportAddress);
        b.append("}");
        return b.toString();
    }

    @Override
    public void stopInput(String inputId) throws IOException, APIException {
        api().inputs().stop(inputId);
    }

    @Override
    public void startInput(String inputId) throws IOException, APIException {
        api().inputs().launchExisting(inputId);
    }

    @Override
    public void restartInput(String inputId) throws IOException, APIException {
        api().inputs().restart(inputId);
    }

    public RadioAPI api() {
        return new RadioAPI(restAdapterFactory.create(this.getTransportAddress()));
    }

    public RadioAPI api(String user, String password) {
        return new RadioAPI(restAdapterFactory.create(this.getTransportAddress(), user, password));
    }
}
