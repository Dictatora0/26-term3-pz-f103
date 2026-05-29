import DEV from '@/env/dev.config.js'
import PROD from '@/env/prod.config.js'

let env = import.meta.env.DEV ? DEV : PROD;
/**
 * @type EnvConfig
 */
export default env;