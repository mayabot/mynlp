package guice;

import com.google.inject.AbstractModule;

public class BillingModule extends AbstractModule {
    @Override
    protected void configure() {
//
//     /*
//      * This tells Guice that whenever it sees a dependency on a TransactionLog,
//      * it should satisfy the dependency using a DatabaseTransactionLog.
//      */
//    bind(TransactionLog.class).to(DatabaseTransactionLog.class);
//
//     /*
//      * Similarly, this binding tells Guice that when CreditCardProcessor is used in
//      * a dependency, that should be satisfied with a PaypalCreditCardProcessor.
//      */
//    bind(CreditCardProcessor.class).to(PaypalCreditCardProcessor.class);
    }
}