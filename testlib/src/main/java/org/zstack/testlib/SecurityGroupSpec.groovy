package org.zstack.testlib

import org.zstack.kvm.KVMAgentCommands
import org.zstack.kvm.KVMSecurityGroupBackend
import org.zstack.sdk.SecurityGroupInventory
import org.zstack.sdk.VmNicInventory

/**
 * Created by xing5 on 2017/2/20.
 */
class SecurityGroupSpec implements Spec, HasSession {
    String name
    String description
    private List<Closure> l3Networks = []
    private List<Closure> vmNics = []

    SecurityGroupInventory inventory

    SpecID create(String uuid, String sessionId) {
        inventory = createSecurityGroup {
            delegate.resourceUuid = uuid
            delegate.name = name
            delegate.description = description
            delegate.sessionId = sessionId
            delegate.userTags = userTags
            delegate.systemTags = systemTags
        }

        l3Networks.each { l3 ->
            attachSecurityGroupToL3Network {
                delegate.sessionId = sessionId
                delegate.securityGroupUuid = inventory.uuid
                delegate.l3NetworkUuid = l3()
            }
        }

        if (!vmNics.isEmpty()) {
            addVmNicToSecurityGroup {
                delegate.sessionId = sessionId
                delegate.securityGroupUuid = inventory.uuid
                delegate.vmNicUuids = vmNics.collect { it() }
            }
        }

        return id(name, inventory.uuid)
    }

    void useL3Network(String...names) {
        names.each { String name ->
            preCreate {
                addDependency(name, L3NetworkSpec.class)
            }

            l3Networks.add {
                L3NetworkSpec l3 = findSpec(name, L3NetworkSpec.class)
                return l3.inventory.uuid
            }
        }
    }

    void useVmNic(String vmName, String l3NetworkName) {
        assert vmName != null: "vmName must be set when calling securityGroup.useVmNic()"
        assert l3NetworkName != null: "l3NetworkName must be set when calling securityGroup.useVmNic()"

        preCreate {
            addDependency(vmName, VmSpec.class)
            addDependency(l3NetworkName, L3NetworkSpec.class)
        }

        vmNics.add {
            VmSpec vm = findSpec(vmName, VmSpec.class)
            L3NetworkSpec l3 = findSpec(l3NetworkName, L3NetworkSpec.class)

            VmNicInventory nic = vm.inventory.vmNics.find { it.l3NetworkUuid == l3.inventory.uuid }
            assert nic!= null: "vm[$name] doesn't have nic on the l3 network[$l3NetworkName], check your environment()"

            return nic.uuid
        }
    }

    SecurityGroupRuleSpec rule(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SecurityGroupRuleSpec.class) Closure c) {
        def spec = new SecurityGroupRuleSpec()
        c.delegate = spec
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        addChild(spec)
        return spec
    }

    static {
        Deployer.simulator(KVMSecurityGroupBackend.SECURITY_GROUP_APPLY_RULE_PATH) {
            return new KVMAgentCommands.ApplySecurityGroupRuleResponse()
        }

        Deployer.simulator(KVMSecurityGroupBackend.SECURITY_GROUP_REFRESH_RULE_ON_HOST_PATH) {
            return new KVMAgentCommands.RefreshAllRulesOnHostResponse()
        }

        Deployer.simulator(KVMSecurityGroupBackend.SECURITY_GROUP_CLEANUP_UNUSED_RULE_ON_HOST_PATH) {
            return new KVMAgentCommands.CleanupUnusedRulesOnHostResponse()
        }
    }
}
