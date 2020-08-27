package org.academiadecodigo.warpers.controller.web;

import org.academiadecodigo.warpers.command.SubscriptionDto;
import org.academiadecodigo.warpers.command.AccountTransactionDto;
import org.academiadecodigo.warpers.converters.AccountDtoToAccount;
import org.academiadecodigo.warpers.converters.UserToUserDto;
import org.academiadecodigo.warpers.exceptions.TransactionInvalidException;
import org.academiadecodigo.warpers.persistence.model.subscription.Subscription;
import org.academiadecodigo.warpers.services.SubscriptionService;
import org.academiadecodigo.warpers.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * Controller responsible for rendering {@link Subscription} related views
 */
@Controller
@RequestMapping("/user")
public class SubscriptionController {

    private UserService userService;
    private SubscriptionService subscriptionService;

    private AccountDtoToAccount accountDtoToAccount;
    private UserToUserDto userToUserDto;

    /**
     * Sets the user service
     *
     * @param userService the user service to set
     */
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Sets the account service
     *
     * @param subscriptionService the account service to set
     */
    @Autowired
    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Sets the converter for converting between account DTO and account model objects
     *
     * @param accountDtoToAccount the account DTO to account converter to set
     */
    @Autowired
    public void setAccountDtoToAccount(AccountDtoToAccount accountDtoToAccount) {
        this.accountDtoToAccount = accountDtoToAccount;
    }

    /**
     * Sets the converter for converting between user model objects and user DTO
     *
     * @param userToUserDto the user to user DTO converter to set
     */
    @Autowired
    public void setUserToUserDto(UserToUserDto userToUserDto) {
        this.userToUserDto = userToUserDto;
    }

    /**
     * Adds an account
     *
     * @param cid                the user id
     * @param subscriptionDto         the account data transfer object
     * @param bindingResult      the binding result object
     * @param redirectAttributes the redirect attributes object
     * @return the view to render
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, path = {"/{cid}/account"})
    public String addAccount(@PathVariable Integer cid, @Valid @ModelAttribute("account") SubscriptionDto subscriptionDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            return "redirect:/user/" + cid;
        }

        try {
            Subscription subscription = accountDtoToAccount.convert(subscriptionDto);
            userService.addAccount(cid, subscription);
            redirectAttributes.addFlashAttribute("lastAction", "Created " + subscription.getAccountType() + " subscription.");
            return "redirect:/user/" + cid;

        } catch (TransactionInvalidException ex) {
            redirectAttributes.addFlashAttribute("failure", "Savings account must have a minimum value of 100 at all times");
            return "redirect:/user/" + cid;
        }
    }

    /**
     * Closes an account
     *
     * @param cid                the user id
     * @param aid                the account id
     * @param redirectAttributes the redirect attributes object
     * @return the view to render
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{cid}/account/{aid}/close")
    public String closeAccount(@PathVariable Integer cid, @PathVariable Integer aid, RedirectAttributes redirectAttributes) throws Exception {

        try {
            userService.closeAccount(cid, aid);
            redirectAttributes.addFlashAttribute("lastAction", "Closed account " + aid);
            return "redirect:/user/" + cid;

        } catch (TransactionInvalidException ex) {
            redirectAttributes.addFlashAttribute("failure", "Unable to perform closing operation. Account # " + aid + " still has funds");
            return "redirect:/user/" + cid;
        }
    }

}
