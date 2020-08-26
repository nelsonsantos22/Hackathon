package org.academiadecodigo.warpers.controller.web;

import org.academiadecodigo.warpers.command.SubscriptionDto;
import org.academiadecodigo.warpers.command.AccountTransactionDto;
import org.academiadecodigo.warpers.converters.AccountDtoToAccount;
import org.academiadecodigo.warpers.converters.CustomerToCustomerDto;
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
@RequestMapping("/customer")
public class SubscriptionController {

    private UserService userService;
    private SubscriptionService subscriptionService;

    private AccountDtoToAccount accountDtoToAccount;
    private CustomerToCustomerDto customerToCustomerDto;

    /**
     * Sets the customer service
     *
     * @param userService the customer service to set
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
     * Sets the converter for converting between customer model objects and customer DTO
     *
     * @param customerToCustomerDto the customer to customer DTO converter to set
     */
    @Autowired
    public void setCustomerToCustomerDto(CustomerToCustomerDto customerToCustomerDto) {
        this.customerToCustomerDto = customerToCustomerDto;
    }

    /**
     * Adds an account
     *
     * @param cid                the customer id
     * @param subscriptionDto         the account data transfer object
     * @param bindingResult      the binding result object
     * @param redirectAttributes the redirect attributes object
     * @return the view to render
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, path = {"/{cid}/account"})
    public String addAccount(@PathVariable Integer cid, @Valid @ModelAttribute("account") SubscriptionDto subscriptionDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            return "redirect:/customer/" + cid;
        }

        try {
            Subscription subscription = accountDtoToAccount.convert(subscriptionDto);
            userService.addAccount(cid, subscription);
            redirectAttributes.addFlashAttribute("lastAction", "Created " + subscription.getAccountType() + " subscription.");
            return "redirect:/customer/" + cid;

        } catch (TransactionInvalidException ex) {
            redirectAttributes.addFlashAttribute("failure", "Savings account must have a minimum value of 100 at all times");
            return "redirect:/customer/" + cid;
        }
    }

    /**
     * Deposits a given amount to an account
     *
     * @param cid                   the customer id
     * @param accountTransactionDto the account transaction data transfer object
     * @param bindingResult         the binding result object
     * @param redirectAttributes    the redirect attributes object
     * @return the view to render
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, path = {"/{cid}/deposit"})
    public String deposit(@PathVariable Integer cid, @Valid @ModelAttribute("transaction") AccountTransactionDto accountTransactionDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("failure", "Deposit failed missing information");
            return "redirect:/customer/" + cid;
        }

        subscriptionService.deposit(accountTransactionDto.getId(), cid, Double.parseDouble(accountTransactionDto.getAmount()));
        redirectAttributes.addFlashAttribute("lastAction", "Deposited " + accountTransactionDto.getAmount() + " into account # " + accountTransactionDto.getId());
        return "redirect:/customer/" + cid;
    }

    /**
     * Withdraws a given amount from an account
     *
     * @param cid                   the customer id
     * @param accountTransactionDto the account transaction data transfer object
     * @param bindingResult         the binding result object
     * @param redirectAttributes    the redirect attributes object
     * @return the view to render
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST, path = {"/{cid}/withdraw"})
    public String withdraw(@PathVariable Integer cid, @Valid @ModelAttribute("transaction") AccountTransactionDto accountTransactionDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            //this message appears when the form is submitted with value blank
            redirectAttributes.addFlashAttribute("failure", "Withdraw failed missing information");
            return "redirect:/customer/" + cid;
        }

        try {
            subscriptionService.withdraw(accountTransactionDto.getId(), cid, Double.parseDouble(accountTransactionDto.getAmount()));
            redirectAttributes.addFlashAttribute("lastAction", "Withdrew " + accountTransactionDto.getAmount() + " from account # " + accountTransactionDto.getId());
            return "redirect:/customer/" + cid;

        } catch (TransactionInvalidException ex) {
            redirectAttributes.addFlashAttribute("failure", "Withdraw failed. " + accountTransactionDto.getAmount() + " is over the current balance for account # " + accountTransactionDto.getId());
            return "redirect:/customer/" + cid;
        }
    }

    /**
     * Closes an account
     *
     * @param cid                the customer id
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
            return "redirect:/customer/" + cid;

        } catch (TransactionInvalidException ex) {
            redirectAttributes.addFlashAttribute("failure", "Unable to perform closing operation. Account # " + aid + " still has funds");
            return "redirect:/customer/" + cid;
        }
    }

}
