# This file was created just to speed up my mappings for the mobile app
# It will not be used again

import json
from Settings.SettingsList import SettingInfos

def addTab(tabs, line):
    name = ""
    if line.startswith("#"):
        name = line.replace("#", "").strip()
        parent_name = ""
        if "." in name:
            split_name = name.split(".")
            parent_name = split_name[0].strip()
            name = split_name[1].strip()
        existing_tab = any(tab['name'] == name for tab in tabs)
        if not existing_tab:
            tabs.append({
                "name": name,
                "text": "",
                "settings": [],
                "parent_tab": parent_name
            })

    return tabs, name

tabs = []
current_tab_name = ""
current_parent_key = ""
with open("Z:/AndroidDev/Projects/ZOoTRBuddy/app/src/main/python/Settings/settingstext.txt", 'r') as file:
    for line in file:
        if line.strip() == "":
            continue
        
        tabs, tab_name = addTab(tabs, line)
        current_tab_name = tab_name if tab_name != "" else current_tab_name
        if not line.startswith("#"):
            current_tab = next((tab for tab in tabs if tab["name"] == current_tab_name), None)
            if current_tab != None:
                if not line.startswith('    '):
                    current_parent_key = line.strip()
                settingsInfos = SettingInfos()
                settingsVars = vars(SettingInfos)[line.strip()]
                gui_type = f"{type(settingsVars)}".replace("<class 'Settings.SettingTypes.", "").replace("'>", "").strip()
                if gui_type == "Scale":
                    scale = getattr(settingsVars, "choices")
                    current_tab["settings"].append({
                        "type": gui_type,
                        "key": line.strip(),
                        "disable": getattr(settingsVars, "disable"),
                        "gui_text": getattr(settingsVars, "gui_text").strip(),
                        "gui_tooltip": getattr(settingsVars, "gui_tooltip").strip(),
                        "choices": {},
                        "default": getattr(settingsVars, "default"),
                        "gui_params": getattr(settingsVars, "gui_params"),
                        "dependent_on": {
                            "parent_key": current_parent_key if line.startswith('    ') else "",
                            "parent_value": ""
                        }
                    })
                else:
                    current_tab["settings"].append({
                        "type": gui_type,
                        "key": line.strip(),
                        "disable": getattr(settingsVars, "disable"),
                        "gui_text": getattr(settingsVars, "gui_text").strip(),
                        "gui_tooltip": getattr(settingsVars, "gui_tooltip").strip(),
                        "choices": getattr(settingsVars, "choices"),
                        "default": getattr(settingsVars, "default"),
                        "gui_params": getattr(settingsVars, "gui_params"),
                        "dependent_on": {
                            "parent_key": current_parent_key if line.startswith('    ') else "",
                            "parent_value": ""
                        }
                    })

with open("Z:/AndroidDev/Projects/ZOoTRBuddy/app/src/main/python/Utils/data/settings_mapping_mobile.json", 'w') as file:
    data = {
        "tabs": tabs
    }
    
    json.dump(data, file, indent=4)
