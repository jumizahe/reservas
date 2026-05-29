package edu.unimagdalena.reservas.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple controller to forward root requests to the SPA index.html
 */
@Controller
public class SpaController {

    @RequestMapping("/")
    public String index() {
        // forward to the static index.html placed under src/main/resources/static
        return "forward:/index.html";
    }
}
