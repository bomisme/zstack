package org.zstack.testlib

import org.springframework.http.HttpEntity
import org.zstack.sdk.PrimaryStorageInventory
import org.zstack.storage.primary.smp.KvmBackend
import org.zstack.utils.gson.JSONObjectUtil

/**
 * Created by xing5 on 2017/2/20.
 */
class SharedMountPointPrimaryStorageSpec extends PrimaryStorageSpec {
    SpecID create(String uuid, String sessionId) {
        inventory = addSharedMountPointPrimaryStorage {
            delegate.resourceUuid = uuid
            delegate.name = name
            delegate.description = description
            delegate.url = url
            delegate.sessionId = sessionId
            delegate.zoneUuid = (parent as ZoneSpec).inventory.uuid
            delegate.userTags = userTags
            delegate.systemTags = systemTags
        } as PrimaryStorageInventory

        return id(name, inventory.uuid)
    }

    static {
        Deployer.simulator(KvmBackend.CONNECT_PATH) { HttpEntity<String> e, EnvSpec spec ->
            def cmd = JSONObjectUtil.toObject(e.body, KvmBackend.ConnectCmd.class)
            SharedMountPointPrimaryStorageSpec sspec = spec.specByUuid(cmd.uuid)
            assert sspec != null: "cannot find shared mount point storage[uuid:${cmd.uuid}]"

            def rsp = new KvmBackend.AgentRsp()
            rsp.totalCapacity = sspec.totalCapacity
            rsp.availableCapacity = sspec.availableCapacity
            return rsp
        }

        Deployer.simulator(KvmBackend.CREATE_VOLUME_FROM_CACHE_PATH) {
            return new KvmBackend.AgentRsp()
        }

        Deployer.simulator(KvmBackend.DELETE_BITS_PATH) {
            return new KvmBackend.AgentRsp()
        }

        Deployer.simulator(KvmBackend.CREATE_TEMPLATE_FROM_VOLUME_PATH) {
            return new KvmBackend.AgentRsp()
        }

        Deployer.simulator(KvmBackend.UPLOAD_BITS_TO_SFTP_BACKUPSTORAGE_PATH) {
            return new KvmBackend.AgentRsp()
        }

        Deployer.simulator(KvmBackend.DOWNLOAD_BITS_FROM_SFTP_BACKUPSTORAGE_PATH) {
            return new KvmBackend.AgentRsp()
        }

        Deployer.simulator(KvmBackend.REVERT_VOLUME_FROM_SNAPSHOT_PATH) {
            def rsp = new KvmBackend.RevertVolumeFromSnapshotRsp()
            rsp.newVolumeInstallPath = "/new/path"
            return rsp
        }

        Deployer.simulator(KvmBackend.MERGE_SNAPSHOT_PATH) {
            return new KvmBackend.MergeSnapshotRsp()
        }

        Deployer.simulator(KvmBackend.GET_VOLUME_SIZE_PATH) {
            def rsp = new KvmBackend.GetVolumeSizeRsp()
            rsp.actualSize = 0
            rsp.size = 0
            return rsp
        }

        Deployer.simulator(KvmBackend.OFFLINE_MERGE_SNAPSHOT_PATH) {
            return new KvmBackend.AgentRsp()
        }

        Deployer.simulator(KvmBackend.CREATE_EMPTY_VOLUME_PATH) {
            return new KvmBackend.AgentRsp()
        }

        Deployer.simulator(KvmBackend.CHECK_BITS_PATH){
            def rsp = new KvmBackend.CheckBitsRsp()
            rsp.existing = true
            return rsp
        }
    }
}
