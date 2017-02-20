package org.zstack.testlib

import org.springframework.http.HttpEntity
import org.zstack.core.Platform
import org.zstack.core.db.Q
import org.zstack.kvm.KVMAgentCommands
import org.zstack.sdk.PrimaryStorageInventory
import org.zstack.storage.ceph.primary.CephPrimaryStorageBase
import org.zstack.storage.ceph.primary.CephPrimaryStorageMonBase
import org.zstack.storage.ceph.primary.CephPrimaryStorageMonVO
import org.zstack.storage.ceph.primary.CephPrimaryStorageMonVO_
import org.zstack.utils.gson.JSONObjectUtil

/**
 * Created by xing5 on 2017/2/20.
 */
class CephPrimaryStorageSpec extends PrimaryStorageSpec {
    String fsid
    List<String> monUrls
    Map<String, String> monAddrs = [:]

    SpecID create(String uuid, String sessionId) {
        inventory = addCephPrimaryStorage {
            delegate.resourceUuid = uuid
            delegate.name = name
            delegate.description = description
            delegate.url = url
            delegate.sessionId = sessionId
            delegate.zoneUuid = (parent as ZoneSpec).inventory.uuid
            delegate.userTags = userTags
            delegate.systemTags = systemTags
            delegate.monUrls = monUrls
        } as PrimaryStorageInventory

        return id(name, inventory.uuid)
    }

    static {
        Deployer.simulator(CephPrimaryStorageBase.GET_FACTS) { HttpEntity<String> e, EnvSpec spec ->
            CephPrimaryStorageBase.GetFactsCmd cmd = JSONObjectUtil.toObject(e.body, CephPrimaryStorageBase.GetFactsCmd.class)
            CephPrimaryStorageSpec cspec = spec.specByUuid(cmd.uuid)
            assert cspec != null: "cannot find ceph primary storage[uuid:${cmd.uuid}], check your environment()"

            def rsp = new CephPrimaryStorageBase.GetFactsRsp()
            rsp.fsid = cspec.fsid

            String monAddr = Q.New(CephPrimaryStorageMonVO.class)
                    .select(CephPrimaryStorageMonVO_.monAddr).eq(CephPrimaryStorageMonVO_.uuid, cmd.monUuid).findValue()

            rsp.monAddr = monAddrs[(monAddr)]

            return rsp
        }

        Deployer.simulator(CephPrimaryStorageBase.DELETE_POOL_PATH) {
            return new CephPrimaryStorageBase.DeletePoolRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.INIT_PATH) { HttpEntity<String> e, EnvSpec spec ->
            def cmd = JSONObjectUtil.toObject(e.body, CephPrimaryStorageBase.InitCmd.class)
            CephPrimaryStorageSpec cspec = spec.specByUuid(cmd.uuid)
            assert cspec != null: "cannot find ceph primary storage[uuid:${cmd.uuid}], check your environment()"

            def rsp = new CephPrimaryStorageBase.InitRsp()
            rsp.fsid = cspec.fsid
            rsp.userKey = Platform.uuid
            rsp.totalCapacity = totalCapacity
            rsp.availableCapacity = availableCapacity
            return rsp
        }

        Deployer.simulator(CephPrimaryStorageMonBase.PING_PATH) {
            return new CephPrimaryStorageMonBase.PingRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.CREATE_VOLUME_PATH) {
            return new CephPrimaryStorageBase.CreateEmptyVolumeRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.KVM_CREATE_SECRET_PATH) {
            return new  KVMAgentCommands.AgentResponse()
        }

        Deployer.simulator(CephPrimaryStorageBase.DELETE_PATH) {
            return new CephPrimaryStorageBase.DeleteRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.CREATE_SNAPSHOT_PATH) {
            def rsp = new CephPrimaryStorageBase.CreateSnapshotRsp()
            rsp.size = 0
            return rsp
        }

        Deployer.simulator(CephPrimaryStorageBase.DELETE_SNAPSHOT_PATH) {
            return new CephPrimaryStorageBase.DeleteSnapshotRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.PROTECT_SNAPSHOT_PATH) {
            return new CephPrimaryStorageBase.ProtectSnapshotRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.UNPROTECT_SNAPSHOT_PATH) {
            return new CephPrimaryStorageBase.UnprotectedSnapshotRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.CLONE_PATH) {
            return new CephPrimaryStorageBase.CloneRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.FLATTEN_PATH) {
            return new CephPrimaryStorageBase.FlattenRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.CP_PATH) {
            def rsp = new CephPrimaryStorageBase.CpRsp()
            rsp.size = 0
            rsp.actualSize = 0
            return rsp
        }

        Deployer.simulator(CephPrimaryStorageBase.GET_VOLUME_SIZE_PATH) {
            def rsp = new CephPrimaryStorageBase.GetVolumeSizeRsp()
            rsp.actualSize = 0
            rsp.size = 0
            return rsp
        }

        Deployer.simulator(CephPrimaryStorageBase.ROLLBACK_SNAPSHOT_PATH) {
            return new CephPrimaryStorageBase.RollbackSnapshotRsp()
        }

        Deployer.simulator(CephPrimaryStorageBase.KVM_HA_SETUP_SELF_FENCER) {
            return new CephPrimaryStorageBase.AgentResponse()
        }

        Deployer.simulator(CephPrimaryStorageBase.KVM_HA_CANCEL_SELF_FENCER) {
            return new CephPrimaryStorageBase.AgentResponse()
        }

        Deployer.simulator(CephPrimaryStorageBase.DELETE_IMAGE_CACHE) {
            return new CephPrimaryStorageBase.AgentResponse()
        }
    }
}
