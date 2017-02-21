package org.zstack.testlib

import org.zstack.sdk.AttachL2NetworkToClusterAction
import org.zstack.sdk.AttachPrimaryStorageToClusterAction
import org.zstack.sdk.ClusterInventory

/**
 * Created by xing5 on 2017/2/12.
 */
class ClusterSpec implements Spec {
    String name
    String description
    String hypervisorType
    List<HostSpec> hosts = []

    private List<String> primaryStorageToAttach = []
    private List<String> l2NetworkToAttach = []

    ClusterInventory inventory

    ClusterSpec() {
    }

    ClusterSpec(String name, String description, String hypervisorType) {
        this.name = name
        this.description = description
        this.hypervisorType = hypervisorType
    }

    KVMHostSpec kvm(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = KVMHostSpec.class) Closure c) {
        def hspec = new KVMHostSpec()
        c.delegate = hspec
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        addChild(hspec)
        hosts.add(hspec)
        return hspec
    }

    void attachPrimaryStorage(String... names) {
        names.each { String primaryStorageName ->
            preCreate {
                addDependency(primaryStorageName, PrimaryStorageSpec.class)
            }

            primaryStorageToAttach.add(primaryStorageName)
        }
    }

    void attachL2Network(String ...names) {
        names.each { String l2NetworkName ->
            preCreate {
                addDependency(l2NetworkName, L2NetworkSpec.class)
            }

            l2NetworkToAttach.add(l2NetworkName)
        }
    }

    SpecID create(String uuid, String sessionId) {
        inventory = createCluster {
            delegate.resourceUuid = uuid
            delegate.name = name
            delegate.description = description
            delegate.hypervisorType = hypervisorType
            delegate.zoneUuid = (parent as ZoneSpec).inventory.uuid
            delegate.sessionId = sessionId
            delegate.userTags = userTags
            delegate.systemTags = systemTags
        } as ClusterInventory

        primaryStorageToAttach.each { String primaryStorageName ->
            def ps = findSpec(primaryStorageName, PrimaryStorageSpec.class) as PrimaryStorageSpec
            def a = new AttachPrimaryStorageToClusterAction()
            a.clusterUuid = inventory.uuid
            a.primaryStorageUuid = ps.inventory.uuid
            a.sessionId = Test.deployer.envSpec.session.uuid
            errorOut(a.call())
        }

        l2NetworkToAttach.each { String l2NetworkName ->
            def l2 = findSpec(l2NetworkName, L2NetworkSpec.class) as L2NetworkSpec
            def a = new AttachL2NetworkToClusterAction()
            a.clusterUuid = inventory.uuid
            a.l2NetworkUuid = l2.inventory.uuid
            a.sessionId = Test.deployer.envSpec.session.uuid
            errorOut(a.call())
        }

        postCreate {
            inventory = queryCluster {
                conditions=["uuid=${inventory.uuid}".toString()]
            }[0]
        }

        return id(name, inventory.uuid)
    }
}
