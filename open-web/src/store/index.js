import {createStore} from 'vuex';
import modules from './modules';
import * as getters from './getters'

const store = createStore({
    modules: modules,
    getters,

})
export default store;
