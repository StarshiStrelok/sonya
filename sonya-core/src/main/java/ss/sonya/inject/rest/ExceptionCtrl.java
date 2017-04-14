/*
 * Copyright (C) 2016 SS
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ss.sonya.inject.rest;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * REST web-services interceptor for handle exceptions.
 * @author SS
 */
@ControllerAdvice
public class ExceptionCtrl {
    /** Logger. */
    private static final Logger LOG =
            Logger.getLogger(ExceptionCtrl.class);
    /**
     * Exception handle method.
     * @param exception exception.
     * @return standard json message.
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> exception(final Exception exception) {
        LOG.error("web-service exception!", exception);
        Map<String, Object> map = new HashMap<>();
        map.put("error", true);
        map.put("message", exception.getMessage());
        return map;
    }
}
