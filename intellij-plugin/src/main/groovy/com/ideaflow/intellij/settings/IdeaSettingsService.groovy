package com.ideaflow.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
        name = "IdeaflowSettings",
        storages = @Storage(id="other", file = '$APP_CONFIG$/other.xml')
)
class IdeaSettingsService implements PersistentStateComponent<IdeaSettingsService> {

    String taskId

    String user

    String project

    String urlValue

    String urlOverrideValue


    @Override
    IdeaSettingsService getState() {
        return this;
    }

    @Override
    void loadState(IdeaSettingsService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

