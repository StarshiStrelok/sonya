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


export class OSRMResponse {
    constructor(
        public code: string,
        public routes: OSRMRoute[]
    ) {}
}
export class OSRMRoute {
    constructor(
        public distance: number,
        public duration: number,
        public geometry: OSRMGeometry,
        public legs: OSRMLeg[]
    ) {}
}
export class OSRMGeometry {
    constructor(
        public type: string,
        public coordinates: number[][]
    ) {}
}
export class OSRMLeg {
    constructor(
        public distance: number,
        public duration: number,
        public summary: string,
        public steps: OSRMStep[]
    ) {}
}
export class OSRMStep {
    constructor(
        public distance: number,
        public duration: number,
        public geometry: OSRMGeometry
    ) {}
}