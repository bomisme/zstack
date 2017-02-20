import org.zstack.testlib.DiskOfferingSpec
import org.zstack.testlib.EnvSpec
import org.zstack.testlib.Test
import org.zstack.testlib.premium.TestPremium
import org.zstack.utils.data.SizeUnit

/**
 * Created by xing5 on 2017/2/12.
 * 1. 2
 * 3
 */
class Test1 extends TestPremium {
    boolean success
    DiskOfferingSpec diskOfferingSpec
    EnvSpec envSpec

    @Override
    void setup() {
        spring {
            nfsPrimaryStorage()
            kvm()
            vyos()
            eip()
            sftpBackupStorage()
            portForwarding()
            lb()
            ipsec()
        }
    }

    @Override
    void environment() {
        envSpec = env {
            account {
                name = "xin"
                password = "password"
            }

            diskOffering {
                name = "diskOffering"
                diskSize = SizeUnit.GIGABYTE.toByte(10)
                useAccount("xin")
            }

            instanceOffering {
                name = "instanceOffering"
                memory = SizeUnit.GIGABYTE.toByte(8)
                cpu = 4
                useAccount("xin")
            }

            sftpBackupStorage {
                name = "sftp"
                url = "/sftp"
                username = "root"
                password = "password"
                hostname = "localhost"

                image {
                    name = "image1"
                    url  = "http://zstack.org/download/test.qcow2"
                    useAccount("xin")
                }

                image {
                    name = "vr"
                    url  = "http://zstack.org/download/vr.qcow2"
                    useAccount("xin")
                }
            }

            zone {
                name = "zone"
                description = "test"

                cluster {
                    name = "cluster"
                    hypervisorType = "KVM"

                    kvm {
                        name = "kvm"
                        managementIp = "localhost"
                        username = "root"
                        password = "password"
                        usedMem = 1000
                        totalCpu = 10
                    }

                    attachPrimaryStorage("nfs")
                    attachL2Network("l2")
                }

                nfsPrimaryStorage {
                    name = "nfs"
                    url = "localhost:/nfs"
                }

                l2NoVlanNetwork {
                    name = "l2"
                    physicalInterface = "eth0"

                    l3Network {
                        name = "l3"
                        useAccount("xin")

                        service {
                            provider = "vrouter"
                            types = ["DHCP", "DNS", "Eip", "SNAT", "PortForwarding", "LoadBalancer", "IPsec"]
                        }

                        ip {
                            startIp = "192.168.100.10"
                            endIp = "192.168.100.100"
                            netmask = "255.255.255.0"
                            gateway = "192.168.100.1"
                        }
                    }

                    l3Network {
                        name = "pubL3"
                        useAccount("xin")

                        ip {
                            startIp = "12.16.10.10"
                            endIp = "12.16.10.100"
                            netmask = "255.255.255.0"
                            gateway = "12.16.10.1"
                        }
                    }
                }

                virtualRouterOffering {
                    name = "vr"
                    memory = SizeUnit.MEGABYTE.toByte(512)
                    cpu = 2
                    useManagementL3Network("pubL3")
                    usePublicL3Network("pubL3")
                    useImage("vr")
                    useAccount("xin")
                }

                attachBackupStorage("sftp")

                eip {
                    name = "eip"
                    useVip("pubL3")
                    useVmNic("vm", "l3")
                    useAccount("xin")
                }

                portForwarding {
                    name = "pf"
                    vipPortStart = 22
                    privatePortStart = 22
                    protocolType = "TCP"
                    useVip("pubL3")
                    useVmNic("vm", "l3")
                    useAccount("xin")
                }

                lb {
                    name = "lb"
                    useVip("pubL3")
                    useAccount("xin")

                    listener {
                        protocol = "tcp"
                        loadBalancerPort = 22
                        instancePort = 22
                        useVmNic("vm", "l3")
                    }
                }

                ipsec {
                    name = "ipsec"
                    peerAddress = "1.1.1.1"
                    peerCidrs = ["10.10.0.0/24"]
                    useVip("pubL3")
                    useL3Network("l3")
                }
            }

            vm {
                name = "vm"
                useInstanceOffering("instanceOffering")
                useImage("image1")
                useL3Networks("l3")
                useAccount("xin")
            }
        }.create()
    }

    @Override
    void test() {
        diskOfferingSpec = envSpec.find("diskOffering", DiskOfferingSpec.class)
        println("${diskOfferingSpec.name}")
    }
}
