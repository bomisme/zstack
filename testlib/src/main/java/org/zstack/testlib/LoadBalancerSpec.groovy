package org.zstack.testlib

import org.zstack.sdk.LoadBalancerInventory
import org.zstack.sdk.VipInventory

/**
 * Created by xing5 on 2017/2/20.
 */
class LoadBalancerSpec implements Spec, HasSession {
    String name
    String description
    private Closure vip

    LoadBalancerInventory inventory

    SpecID create(String uuid, String sessionId) {
        inventory = createLoadBalancer {
            delegate.resourceUuid = uuid
            delegate.name = name
            delegate.description = description
            delegate.vipUuid = vip(sessionId)
            delegate.userTags = userTags
            delegate.systemTags = systemTags
            delegate.sessionId = sessionId
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

    LoadBalancerListenerSpec listener(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = LoadBalancerListenerSpec.class) Closure c) {
        def spec = new LoadBalancerListenerSpec()
        c.delegate = spec
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        addChild(spec)
        return spec
    }
}
