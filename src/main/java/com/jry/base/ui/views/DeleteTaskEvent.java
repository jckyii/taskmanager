package com.jry.base.ui.views; // Adjust package name to match your other files

import com.jry.base.ui.components.TaskCard;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;

/**
 * Custom event fired when the quick-delete trash can button is clicked on a TaskCard.
 */
public class DeleteTaskEvent extends ComponentEvent<TaskCard> {

    private final Long taskId;

    public DeleteTaskEvent(TaskCard source, Long taskId) {
        // 'false' indicates this is not a 'fromClient' event
        super(source, false);
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }
}

/**
 * Definition for the event listener.
 */
interface DeleteTaskListener extends ComponentEventListener<DeleteTaskEvent> {
    void onDelete(DeleteTaskEvent event);
}