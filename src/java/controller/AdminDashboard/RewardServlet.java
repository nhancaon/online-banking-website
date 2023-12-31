package controller.AdminDashboard;

import business.Customer;
import business.Reward;
import DAO.RewardDAO;
import Exception.HandleException;

import java.io.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

@WebServlet("/reward")
public class RewardServlet extends HttpServlet {

    RewardDAO rewardDAO = new RewardDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext servletContext = getServletContext();
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "join"; // default action
        }

        String url = "/admin-dashboard/";
        switch (action) {
            case "add-reward" -> {
                this.addReward(request, response);
                this.showReward(request, response);
            }
            case "update-reward" -> {
                this.updateReward(request, response);
                this.showReward(request, response);
            }
            case "delete" -> {
                this.deleteReward(request, response);
                this.showReward(request, response);
            }
            default -> {
            }
        }
        url = "/admin-dashboard/reward.jsp";
        servletContext.getRequestDispatcher(url).forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext servletContext = getServletContext();

        String action = request.getParameter("action");
        if (action == null) {
            action = "join"; // default action
        }

        String url = "/admin-dashboard/";
        switch (action) {
            case "show-reward" ->
                this.showReward(request, response);
            default -> {
            }
        }
        url = "/admin-dashboard/reward.jsp";
        servletContext.getRequestDispatcher(url).forward(request, response);
    }

    protected void showReward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Reward> rewards = rewardDAO.findAllReward();
        request.setAttribute("rewards", rewards);
    }

    protected void addReward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("rewardName");
        String type = request.getParameter("rewardType");
        int costPoint = Integer.parseInt(request.getParameter("costPoint"));

        rewardDAO.createReward(name, costPoint, type);
        request.setAttribute("successMessage", "The reward has been added successfully.");
    }

    protected void updateReward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rewardId = request.getParameter("rewardIdUpdate");
        Reward reward = rewardDAO.findByRewardId(rewardId);

        String rewardName, rewardType;
        int costPoint;

        if(!request.getParameter("rewardNameUpdate").isEmpty()){
            rewardName = request.getParameter("rewardNameUpdate");
        }else{
            rewardName = reward.getRewardName();
        }

        if(!request.getParameter("rewardTypeUpdate").isEmpty()){
            rewardType = request.getParameter("rewardTypeUpdate");
        }else{
            rewardType = reward.getRewardType();
        }

        if(!request.getParameter("costPointUpdate").isEmpty()){
            costPoint = Integer.parseInt(request.getParameter("costPointUpdate"));
        }else{
            costPoint = reward.getCostPoint();
        }

        try {
            rewardDAO.checkUpdateReward(rewardId, rewardName, rewardType, costPoint);
            request.setAttribute("successMessage", "The reward has been updated successfully.");

        } catch (HandleException e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
    }

    protected void deleteReward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rewardId = request.getParameter("rewardId");
        rewardDAO.delete(rewardId);
    }
}
