package cn.rpc.lite.core.enums;

/**
 * Zookeeper常量
 */
public interface ZkConfig {

    int SESSION_TIMEOUT = 5000;

    String REGISTRY_PATH = "/registry";

    String DATA_PATH = REGISTRY_PATH + "/data";
}
