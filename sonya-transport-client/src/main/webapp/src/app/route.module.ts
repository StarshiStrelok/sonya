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

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TransportProfileForm} from './form/transport-profile/transport-profile.form';
import {TransportProfileList} from './component/transport-profile-list/transport-profile.list';

const routes: Routes = [
    {path: '', redirectTo: 'ui/admin/profile-list', pathMatch: 'full'},
    {path: 'ui/admin/profile-list', component: TransportProfileList},
    {path: 'ui/admin/profile', component: TransportProfileForm}
];
@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {}
