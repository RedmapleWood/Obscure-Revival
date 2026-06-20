/*
Obscure Revival © 2026 by Redmaple Wood is licensed under CC BY-NC-SA 4.0.
To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/
(Optional but appreciated: If you include this mod in a modpack, consider letting me know!)
*/

package redmaple.obscurerevival.refined_chain_armor;

public interface KeloidStateAccess {
    int obscure_revival$getKeloidTimer();
    void obscure_revival$setKeloidTimer(int timer);

    int obscure_revival$getKeloidFailCount();
    void obscure_revival$setKeloidFailCount(int count);
}