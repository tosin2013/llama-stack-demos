import streamlit as st

st.title("Llama Stack + MCP Client")

with st.sidebar:
    st.header("MCP Servers")
    tool_groups = ["Ansible","OpenShift","GitHub"]
    toolgroup_selection = st.pills(label="Available Servers",options=tool_groups, selection_mode="multi",)

    tool_list = ["tool_1","tool_2","tool_3"]
    
    active_tool_list = []
    for toolgroup_id in toolgroup_selection:
        active_tool_list.extend([f"{toolgroup_id}_{tool}" for tool in tool_list])
    st.markdown("Active Tools: ")
    st.json(active_tool_list)

    st.text_input(label="Install New Server", placeholder="MCP Server")


if "messages" not in st.session_state:
    st.session_state["messages"] = [{"role": "assistant", "content": "How can I help you?"}]

for msg in st.session_state.messages:
    st.chat_message(msg["role"]).write(msg["content"])

if prompt := st.chat_input(placeholder=""):
    st.chat_message("user").write(prompt)
    st.chat_message("assistant").write(prompt)
