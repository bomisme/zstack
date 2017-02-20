package org.zstack.testlib

import org.zstack.core.Platform
import org.zstack.sdk.AbstractAction

import java.lang.reflect.Modifier

/**
 * Created by xing5 on 2017/2/14.
 */
class CreationSpecGenerator {
    Set<Class> actions
    List<String> groovyActions = []

    CreationSpecGenerator() {
        def reflections = Platform.getReflections()
        actions = reflections.getSubTypesOf(AbstractAction.class)
    }

    String generate(String outputFilePath) {
        actions.each { actionClass ->
            if (Modifier.isAbstract(actionClass.modifiers)) {
                return
            }

            String funcName = actionClass.simpleName - "Action"
            funcName = "${Character.toLowerCase(funcName.charAt(0))}${funcName.substring(1)}"
            groovyActions.add("""\
    def $funcName(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ${actionClass.typeName}.class) Closure c) {
        def a = new ${actionClass.typeName}()
        ${actionClass.fields.find {it.name == "sessionId"} != null ? "a.sessionId = Test.deployer.envSpec.session?.uuid" : ""}
        def code = c.rehydrate(a, this, this)
        code()
        return errorOut(a.call())
    }

""")
        }

        def fileContent = """package org.zstack.testlib

import org.zstack.utils.gson.JSONObjectUtil

trait CreationSpec {
    def errorOut(res) {
        assert res.error == null : "API failure: \${JSONObjectUtil.toJsonString(res.error)}"
        if (res.value.hasProperty("inventory")) {
            return res.value.inventory
        } else {
            return res.value.inventories
        }
    }
    
    ${groovyActions.join("\n")}
}
"""
        def dir = new File(outputFilePath).parentFile
        if (!dir.exists()) {
            dir.mkdirs()
        }

        new File(outputFilePath).write(fileContent)
    }
}