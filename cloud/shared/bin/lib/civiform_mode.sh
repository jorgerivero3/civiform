#######################################
# Returns
# Globals:
#  CIVIFORM_MODE
#######################################
function civiform_mode::is_test() {
  [[ "${CIVIFORM_MODE}" == "test" ]]
}
