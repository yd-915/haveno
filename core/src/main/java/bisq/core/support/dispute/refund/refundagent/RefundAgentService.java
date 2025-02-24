/*
 * This file is part of Haveno.
 *
 * Haveno is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Haveno is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Haveno. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.support.dispute.refund.refundagent;

import bisq.core.filter.FilterManager;
import bisq.core.support.dispute.agent.DisputeAgentService;

import bisq.network.p2p.NodeAddress;
import bisq.network.p2p.P2PService;

import com.google.inject.Singleton;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class RefundAgentService extends DisputeAgentService<RefundAgent> {
    @Inject
    public RefundAgentService(P2PService p2PService, FilterManager filterManager) {
        super(p2PService, filterManager);
    }

    @Override
    protected Set<RefundAgent> getDisputeAgentSet(List<String> bannedDisputeAgents) {
        return p2PService.getDataMap().values().stream()
                .filter(data -> data.getProtectedStoragePayload() instanceof RefundAgent)
                .map(data -> (RefundAgent) data.getProtectedStoragePayload())
                .filter(a -> bannedDisputeAgents == null ||
                        !bannedDisputeAgents.contains(a.getNodeAddress().getFullAddress()))
                .collect(Collectors.toSet());
    }

    @Override
    protected List<String> getDisputeAgentsFromFilter() {
        return filterManager.getFilter() != null ? filterManager.getFilter().getRefundAgents() : new ArrayList<>();
    }

    public Map<NodeAddress, RefundAgent> getRefundAgents() {
        return super.getDisputeAgents();
    }
}
