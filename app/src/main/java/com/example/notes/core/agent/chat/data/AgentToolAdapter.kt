package com.example.notes.core.agent.chat.data

import android.util.Log
import com.example.notes.core.agent.chat.domain.AgentTool
import com.example.notes.core.agent.chat.domain.ToolParameter
import com.google.ai.edge.litertlm.OpenApiTool
import org.json.JSONObject
import org.json.JSONArray

class AgentToolAdapter(
    private val tool: AgentTool
) : OpenApiTool {
    override fun getToolDescriptionJsonString(): String {
        val properties =
            JSONObject()
        tool.parameters.forEach {
            properties.put(
                it.name,
                it.toSchema()
            )
        }

        val required = JSONArray(tool.parameters.filter { it.isRequired }.map { it.name })

        val schema = JSONObject()
            .put("name", tool.name)
            .put("description", tool.description)
            .put(
                "parameters",
                JSONObject()
                    .put("type", "object")
                    .put("properties", properties)
                    .put("required", required)
            ).toString()

        Log.d("AGENT", schema)
        return schema
    }

    override fun execute(paramsJsonString: String): String {
        return ""
    }
}

private fun ToolParameter.toSchema(): JSONObject {
    val schema = JSONObject()
        .put("type", type.name.lowercase())
        .put("description", description)

    if (type == ToolParameter.Type.OBJECT) {
        val nested = JSONObject()
        properties.forEach { nested.put(it.name, it.toSchema()) }
        schema.put("properties", nested)
        schema.put("required", JSONArray(properties.filter { it.isRequired }.map { it.name }))
    }

    items?.let { schema.put("items", it.toSchema()) }

    return schema
}
