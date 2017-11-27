package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

/**
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less PEs in use. It is therefore a Worst Fit policy, allocating VMs into the 
 * host with most available PE.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */

public class LBVCFC extends VmAllocationPolicy {

	/** The map between each VM and its allocated host.
         * The map key is a VM UID and the value is the allocated host for that VM. */
	private Map<String, Host> vmTable;

	/** The map between each VM and the number of Pes used. 
         * The map key is a VM UID and the value is the number of used Pes for that VM. */
	private Map<String, Integer> usedPes;

	/** The number of free Pes for each host from {@link #getHostList() }. */
	private List<Integer> freePes;

	/**
	 * Creates a new VmAllocationPolicySimple object.
	 * 
	 * @param list1 the list of hosts
	 * @pre $none
	 * @post $none
	 */

	public LBVCFC(List<? extends Host> list) {
		
		super(list);

		setFreePes(new ArrayList<Integer>());
		for (Host host : getHostList()) {
			getFreePes().add(host.getNumberOfPes());

		}

		setVmTable(new HashMap<String, Host>());
		setUsedPes(new HashMap<String, Integer>());
	}

	
	
	//-------- helper functions to edit ---------------	
	
	public void loadBalancer(List<Vm> vmlist) {
		
		// getHostList() to get list of hosts

		Log.printLine("-----------------------------------------");
		/*for (Host host : getHostList()) {
	
			Log.printLine(host);
		
		}*/
		
//		for(Vm vm : vmlist) {
//		Log.printLine(vm.getUid()+" "+vm.getHost());
//		}
		
		
		Log.printLine("-----------------------------------------");

		deallocateHostForVm(vmlist.get(0));		
		
		Log.printLine("Migrating the VM: " + vmlist.get(0).getId() + " from the Host: " + vmlist.get(0).getHost().getId());
		
		allocateHostForVm(vmlist.get(0));
		
		Log.printLine(" to the Host: " + vmlist.get(0).getHost().getId());
		
		
		Log.printLine("-----------------------------------------");
		deallocateHostForVm(vmlist.get(7));		
		Log.printLine("Migrating the VM: " + vmlist.get(7).getId() + " from the Host: " + vmlist.get(7).getHost().getId());

		allocateHostForVm(vmlist.get(7));
		Log.printLine(" to the Host: " + vmlist.get(7).getHost().getId());		

		
		/*		
		System.out.println("Size of host listin datacenter is: "+getHostList().size());
		System.out.println("Size of vm listin datacenter is: "+vmlist.size());
		
		VMLoadBalancingFuzzy vmAllocationPolicyCustomized=new VMLoadBalancingFuzzy();
		ArrayList<Integer> maptemp=new ArrayList<Integer>();
		maptemp=vmAllocationPolicyCustomized.allocationForMigration(getHostList(),vmlist);
			
		int host_id=0;
		int vm_id=0;
		System.out.println("Size of maptemp: "+maptemp.size());
		for (int i=0;i<maptemp.size();i=i+2){
			vm_id=maptemp.get(i);
			System.out.println("VM id is: "+maptemp.get(i));
			host_id=maptemp.get(i+1);
			Map<String, Object> ma;
			ma=new HashMap<String,Object>();
			ma.put("Host",getHostList().get(host_id));
			ma.put("VM",vmlist.get(vm_id));
			double delay=1.0;
			System.out.println("Migrate");
			send(0,delay,CloudSimTags.VM_MIGRATE,ma);
		*/
		}
		

	/**
	 * Allocates the host with less PEs in use for a given VM.
	 * 
	 * @param vm {@inheritDoc}
	 * @return {@inheritDoc}
	 * @pre $none
	 * @post $none
	 */
	
	public boolean allocateHostForVm(Vm vm) {
		
		Host host = getHostList().get(5);
		host.vmCreate(vm);
		return true;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		int pes = 1;
		if (host != null) {
			host.vmDestroy(vm);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}
	}	
/*	
	
	@Override
	public boolean allocateHostForVm(Vm vm) {
		
		// no. of pes required by vm
		int requiredPes = vm.getNumberOfPes();
		
		boolean result = false;
		int tries = 0;
		
		// gets free pes 
		List<Integer> freePesTmp = new ArrayList<Integer>();
		
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}

		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {
				
				// we still trying until we find a host or until we try all of them
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				// we want the host with less pes in use
				for (int i = 0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) > moreFree) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}

				Host host = getHostList().get(idx);
				result = host.vmCreate(vm);

				if (result) { 
					
					// if vm were succesfully created in the host
					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes);
					getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
					result = true;
					break;

				} else {

					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreePes().size());

		}

		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}
	}
 */
	
	// ------ helper functions -----
	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 * 
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 * 
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	/**
	 * Gets the used pes.
	 * 
	 * @return the used pes
	 */
	
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	/**
	 * Sets the used pes.
	 * 
	 * @param usedPes the used pes
	 */
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	/**
	 * Gets the free pes.
	 * 
	 * @return the free pes
	 */
	protected List<Integer> getFreePes() {
		return freePes;
	}

	/**
	 * Sets the free pes.
	 * 
	 * @param freePes the new free pes
	 */
	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);

			int requiredPes = vm.getNumberOfPes();
			int idx = getHostList().indexOf(host);
			getUsedPes().put(vm.getUid(), requiredPes);
			getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}
}