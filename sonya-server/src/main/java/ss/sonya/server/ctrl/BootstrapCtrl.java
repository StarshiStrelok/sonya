/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.sonya.server.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Webpack bootstrap.
 * @author ss
 */
@Controller
public class BootstrapCtrl {
    /**
     * Home page.
     * @return home page.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String homePage() {
        return "index.html";
    }
}
