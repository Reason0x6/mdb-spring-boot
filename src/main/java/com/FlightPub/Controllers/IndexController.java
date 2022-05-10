package com.FlightPub.Controllers;

import com.FlightPub.Services.FlightServices;
import com.FlightPub.Services.UserAccountServices;
import com.FlightPub.model.*;
import com.FlightPub.repository.FlightRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class IndexController {
    private UserAccountServices usrServices;
    private UserSession SessionUser = new UserSession(null);
    private FlightServices flightServices;

    @Autowired
    @Qualifier(value = "FlightServices")
    public void setFlightServices(FlightServices flightService) {
        this.flightServices = flightService;
    }

    @Autowired
    @Qualifier(value = "UserAccountServices")
    public void setUserService(UserAccountServices usrService) {
        this.usrServices = usrService;
    }

    @RequestMapping("/")
    public String loadIndex(Model model){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        String today = dateFormat.format(date);

        model.addAttribute("today", today); // Temp/placeholder
        cal.add(Calendar.YEAR, 1);
        date = cal.getTime();
        String max = dateFormat.format(date);
        model.addAttribute("max", max);
        model.addAttribute("usr", SessionUser); // Temp/placeholder
        return "index";
    }

    @RequestMapping("/login")
    public String loadLogin(Model model){
        model.addAttribute("usr", SessionUser); // Temp/placeholder
        return "login";
    }

    @RequestMapping("/logout")
    public String loadLogout(Model model){
        SessionUser = new UserSession(null);
        model.addAttribute("usr", SessionUser); // Temp/placeholder
        return "login";
    }

    @PostMapping("/login")
    public String runLogin(@ModelAttribute LoginRequest req, Model model){
        try {

            UserAccount newUser = usrServices.getById(req.getEmail());

            if(req.getPassword().equals(newUser.getPassword())) {
                System.out.println(true);
                model.addAttribute("method", "post");
                SessionUser = new UserSession(newUser);
                model.addAttribute("usr", SessionUser);
            }else{
                System.out.println(false);
                System.out.println(req.getPassword());
                System.out.println(newUser.getPassword());

                // TODO: Add a 'forgot password' workflow

                model.addAttribute("valid", false);
            }

            model.addAttribute("user", req);

        }catch(Exception e){
            System.out.println(req.getPassword());
            e.printStackTrace();
            model.addAttribute("valid", false);

        }

        return "login";
    }

    @PostMapping("/search")
    public String runSearch(@ModelAttribute BasicSearch search, Model model){

        search.setFlightServices(flightServices);
        List<Flight> flights = search.runBasicSearch();
        model.addAttribute("search", search);
        model.addAttribute("flights", flights);
        model.addAttribute("usr", SessionUser);
        return "search";

        // TODO: Add advanced searches
    }
}
