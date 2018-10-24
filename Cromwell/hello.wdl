task hello { command { echo "Hello world" } }
workflow wf_hello { call hello }
