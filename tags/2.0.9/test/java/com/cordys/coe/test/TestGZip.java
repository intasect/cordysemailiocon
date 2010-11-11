
 /**
 * Copyright 2007 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Email IO Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cordys.coe.test;

import com.cordys.coe.ac.emailio.storage.db.ContentUtil;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestGZip
{
    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            String data = "H4sIAAAAAAAAAK1V/U/bPBD+V6L8tFdb6qTfICdSKIF1ooyVbGLSK70KzrX1aOzMdjbKtP/9PSdpKSDGJq1S5a+7e+653GNTo/hyCcq5LdZCh+7KmPKQEC6uZSVyKDK+ZlIIYEaqDpOAf5VvNA4FCTo+wcMFX1YqM1wK18nKcs1BpzJ0p+dH769cRwowvAAp1pvQNaoCN6IiKyD60T+I+8fD7sA7GAZjr+8fnHjj7rjvxX4vCUbDeDAaDX96xxu05ixt00zS2DkByK8zdkNJHYjmoJnipc0gemS+kA9dnBkS0pTsu1Dkcs0F1GR1tMjWGih5uElLxaXiZhMFPiW7BVXVGnQzOBqLhPFC9/Lj0btkkiLRMjMGlHDMpoTQnSenyRXufsvWFUQP0uI5CMMXHNQh3H2avD+eH82SpHeWsrvZl6k5T5Lu7NIfzI5Xp2fp7PbDXdKfpdNg/uVz98OVH/6rX7OV5AwOX3Ve/0NJg0A1fjQcjLwB0VYdUSe15bZ4tamjFQvdizhNk/m5GwW7CKT1JW0o0jLCmeX8mPns41k6vYjnL3G3WXreS3nKosCq/KVESfupCtA6W4KdmJXMo+QWWGXgQkmGJ5S023UWuswYRK0kNFthM+h9AVyXBYHaH9lbOTS5Nm600qAiJkIQuTGr4dc3OJdqmQl+V6slWzvWRL+R4SUHpKrfTqxNA4C737/7/qCjm7MO6oiSOibVG8EiqyWka6e0Vlhlop6PP6xFu7wnUaAwuVjqp1u2tWHBbyOhbWPX079AnjzFeRYZI/059CWWNRb5ZAXsJrEifQn+yRYWg4uyMg4X5pPtotANutiitmEjLrTJhJVJvaQKGPBvWPwfg6NxP/bjkTfqdbtePx4G3oE/PvF6wSQYB93ByXAY/8R22zpsO24Ppo8o2OBb9U8FdmoBOc8MPLqHf5d8E7BRNmnmjXpwQZ6Bimyzt2LQslJYt19etq0NJXXVkNeujLsatyYdYrM83Ltrtr57It6d4htRQvOA4N2Zzqfnp//Nk4uzeJIgrZ3AdyC/QNveGM/ANcd/ikfuid4XjLTvZvQ/DJQS+EEHAAA=";

            String decompresed = ContentUtil.decompressData(data);

            System.out.println(decompresed);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
