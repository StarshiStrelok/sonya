/*
 * Copyright (C) 2017 ss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ss.sonya.test;

import org.junit.Assert;
import org.junit.Test;
import ss.sonya.configuration.SonyaConfig;
import ss.sonya.constants.SonyaProperty;

/**
 *
 * @author ss
 */
public class SonyaConfigTest extends TestConfig {
    @Test
    public void test() {
        Assert.assertNotNull(SonyaConfig.settingB(SonyaProperty.IS_PRODUCTION_SERVER));
        Assert.assertNotNull(SonyaConfig.setting(SonyaProperty.DS_DRIVER));
        Assert.assertNotNull(SonyaConfig.settingI(SonyaProperty.PROCESSOR_CORES, 2));
    }
}
