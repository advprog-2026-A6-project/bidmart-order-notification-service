package id.ac.ui.cs.advprog.ordernotification.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalViewModelAdvice {

    private final String clarityProjectId;

    public GlobalViewModelAdvice(@Value("${clarity.project-id:}") String clarityProjectId) {
        this.clarityProjectId = clarityProjectId;
    }

    @ModelAttribute("clarityProjectId")
    public String clarityProjectId() {
        return clarityProjectId;
    }
}
