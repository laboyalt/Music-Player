function create_vpn (vpn_name) {
  tell application "System Preferences"
    reveal pane "com.apple.preference.network"
    activate
    tell application "System Events"
      tell process "System Preferences"
        tell window 1
          click button "Add Service"
          tell sheet 1
            -- set location type
            click pop up button 1
            click menu item "VPN" of menu 1 of pop up button 1
            delay 1
            
            -- set connection type
            click pop up button 2
            click menu item "L2TP over IPSec" of menu 1 of pop up button 2
            delay 1
            
            -- set name of the service
            -- for some reason the standard 'set value of text field 1' would not work
            set value of text field 1 to vpn_name
            click button "Create"
          end tell
          click button "Apply"
        end tell
      end tell
    end tell
  end tell
end createVPN
}
function updateVPN(vpn_name, vpnAddress, vpnUsername, vpnPassword, sharedSecret){
  tell application "System Preferences"
    reveal pane "com.apple.preference.network"
    activate
    
    tell application "System Events"
      tell process "System Preferences"
        tell window 1
          repeat with r in rows of table 1 of scroll area 1
            if (value of static text 1 of r as string) contains vpn_name then
              select r
            end if
          end repeat
          
          -- Set the address and username
          tell group 1
            set value of text field 1 to vpnAddress
            set value of text field 2 to vpnUsername
          end tell
          
          tell sheet 1
            click button "Ok"
          end tell
          
          tell group 1
            click button "Authentication Settings…"
          end tell
          
          -- Set password
          tell sheet 1
            set value of text field 1 to vpnPassword
            set value of text field 2 to sharedSecret
            click button "Ok"
          end tell
          
          click button "Apply"
        end tell
      end tell
    end tell
  end tell
end updateVPN
}
function upVPN(vpn_name){
  tell application "System Preferences"
    reveal pane "com.apple.preference.network"
    activate
    tell application "System Events"
      click action pop up button "Service Actions"
      tell group 1
        click button "Connect"
        delay 1
      end tell
    end tell
  end tell
end upVPN

set newFile to ("Macintosh HD:Users:intern:Desktop:VPN MPP.txt")
set theFileContents to paragraphs of (read file newFile)
set vpn_name to text 1 of theFileContents as text
set vpnAddress to text 2 of theFileContents as text
set sharedSecret to text 3 of theFileContents as text

display dialog "Username:" default answer ""
set vpnUsername to text returned of result

display dialog "Password:" default answer ""
set vpnPassword to text returned of result
}

function set_service_order () {
# sets the service order for the connection so routing will work
  IFS=$'\n' #handle spaces as newlines
  local serviceOrder=($(networksetup -listnetworkserviceorder | sed '1d' | awk -F'\\) ' '/\(*\)/ {print $2}'| sed '/^$/d'))
  unset IFS
  # update wifi order to slot 0
  local updated=0
  local retVal=$(indexOf "Wi-Fi" serviceOrder)
  if [[ $retVal -ne 0 ]]; then
    # remove from previous location
    local next=$(($retVal+1))
    serviceOrder=("${serviceOrder[@]:0:retVal}" "${serviceOrder[@]:next}")
    # insert first
    serviceOrder=("Wi-Fi" "${serviceOrder[@]}")
    updated=1
  fi
  # update vpn order to slot 1
  retVal=$(indexOf "$vpn" serviceOrder)
  if [[ $retVal -ne 1 ]]; then
    # remove from previous location
    next=$(($retVal+1))
    serviceOrder=("${serviceOrder[@]:0:retVal}" "${serviceOrder[@]:next}")
    # insert vpn after wifi and lan
    serviceOrder=("${serviceOrder[0]}" "$vpn" "${serviceOrder[@]:1}")
    updated=1
  fi
  #printArr serviceOrder

  # save changes if updated
  if [[ $updated -eq 1 ]]; then
    printf "Setting service order for $vpn \n"
    sudo networksetup -ordernetworkservices "${serviceOrder[@]}"
  else
    printf "Verified $vpn service order \n"
  fi
}