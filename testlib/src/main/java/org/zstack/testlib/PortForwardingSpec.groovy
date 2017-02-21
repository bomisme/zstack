package org.zstack.testlib

import org.zstack.sdk.PortForwardingRuleInventory
import org.zstack.sdk.VipInventory
import org.zstack.sdk.VmNicInventory

/**
 * Created by xing5 on 2017/2/20.
 */
class PortForwardingSpec implements Spec, HasSession {
    String name
    String description
    Integer vipPortStart
    Integer vipPortEnd
    Integer privatePortStart
    Integer privatePortEnd
    String allowedCidr
    String protocolType
    private Closure vip
    private Closure vmNic

    PortForwardingRuleInventory inventory

    SpecID create(String uuid, String sessionId) {
        inventory = createPortForwardingRule {
            delegate.resourceUuid = uuid
            delegate.name = name
            delegate.description = description
            delegate.vipPortStart = vipPortStart
            delegate.vipPortEnd = vipPortEnd
            delegate.privatePortStart = privatePortStart
            delegate.privatePortEnd = privatePortEnd
            delegate.protocolType = protocolType
            delegate.allowedCidr = allowedCidr
            delegate.vipUuid = vip(sessionId)
            delegate.sessionId = sessionId
            delegate.userTags = userTags
            delegate.systemTags = systemTags
            delegate.vmNicUuid = vmNic == null ? null : vmNic()
        }

        postCreate {
            inventory = queryPortForwardingRule {
                conditions=["uuid=${inventory.uuid}".toString()]
            }[0]
        }

        return id(name, inventory.uuid)
    }

    void useVip(String vipL3NetworkName) {
        preCreate {
            addDependency(vipL3NetworkName, L3NetworkSpec.class)
        }

        vip = { String sessionId ->
            def l3 = findSpec(vipL3NetworkName, L3NetworkSpec.class) as L3NetworkSpec

            VipInventory inv = createVip {
                delegate.name = "vip-on-$vipL3NetworkName"
                delegate.l3NetworkUuid = l3.inventory.uuid
                delegate.requiredIp = requiredIp == null ? null : requiredIp
                delegate.sessionId = sessionId
            } as VipInventory

            return inv.uuid
        }
    }

    void useVmNic(String vmName, String l3NetworkName) {
        assert vmName != null: "vmName must be set when calling portForwarding.useVmNic()"
        assert l3NetworkName != null: "l3NetworkName must be set when calling portForwarding.useVmNic()"

        preCreate {
            addDependency(vmName, VmSpec.class)
            addDependency(l3NetworkName, L3NetworkSpec.class)
        }

        vmNic = {
            VmSpec vm = findSpec(vmName, VmSpec.class)
            L3NetworkSpec l3 = findSpec(l3NetworkName, L3NetworkSpec.class)

            VmNicInventory nic = vm.inventory.vmNics.find { it.l3NetworkUuid == l3.inventory.uuid }
            assert nic!= null: "vm[$name] doesn't have nic on the l3 network[$l3NetworkName], check your environment()"

            return nic.uuid
        }
    }

}
